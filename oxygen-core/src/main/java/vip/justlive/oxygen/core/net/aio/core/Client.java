/*
 * Copyright (C) 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package vip.justlive.oxygen.core.net.aio.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import vip.justlive.oxygen.core.util.SystemUtils;

/**
 * aio 客户端
 *
 * @author wubo
 */
public class Client {

  @Getter
  private final GroupContext groupContext;
  private final BeatProcessor beatProcessor;
  private final RetryProcessor retryProcessor;
  private volatile boolean started;
  @Getter
  private Map<Long, ChannelContext> channels = new ConcurrentHashMap<>(4);

  public Client(GroupContext groupContext) {
    this.groupContext = groupContext;
    this.beatProcessor = new BeatProcessor(this);
    this.retryProcessor = new RetryProcessor(this);
  }

  /**
   * 连接服务端，本地随机端口
   *
   * @param host 主机
   * @param port 端口
   * @return channelContext
   * @throws IOException io异常时抛出
   */
  public ChannelContext connect(String host, int port) throws IOException {
    return connect(new InetSocketAddress(host, port));
  }

  /**
   * 连接服务器,本地随机端口
   *
   * @param remote 远程地址
   * @return channelContext
   * @throws IOException io异常时抛出
   */
  public ChannelContext connect(InetSocketAddress remote) throws IOException {
    return connect(remote, new InetSocketAddress(SystemUtils.findAvailablePort()));
  }

  /**
   * 连接服务端，指定绑定地址
   *
   * @param remote 远程地址
   * @param bind 本机绑定地址
   * @return channelContext
   * @throws IOException io异常时抛出
   */
  public ChannelContext connect(InetSocketAddress remote, InetSocketAddress bind)
      throws IOException {
    AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup
        .withThreadPool(groupContext.getGroupExecutor());
    groupContext.setChannelGroup(channelGroup);
    AsynchronousSocketChannel channel = Utils.create(groupContext, bind);
    ChannelContext channelContext = new ChannelContext(groupContext, channel, false);
    channelContext.setServerAddress(remote);
    channel.connect(remote, channelContext, ConnectHandler.INSTANCE);

    channels.put(channelContext.getId(), channelContext);
    if (started) {
      return channelContext;
    }
    started = true;
    if (groupContext.getAioHandler().beat(channelContext) != null) {
      groupContext.getScheduledExecutor()
          .schedule(beatProcessor, groupContext.getBeatInterval(), TimeUnit.MILLISECONDS);
    }
    if (groupContext.isRetryEnabled()) {
      groupContext.getScheduledExecutor()
          .schedule(retryProcessor, groupContext.getRetryInterval(), TimeUnit.MILLISECONDS);
    }
    return channelContext;
  }

  /**
   * 关闭单个channel
   *
   * @param channelContext channel
   */
  public void close(ChannelContext channelContext) {
    channels.remove(channelContext.getId());
    channelContext.close();
  }

  /**
   * 关闭客户端
   */
  public void close() {
    for (ChannelContext channelContext : channels.values()) {
      channelContext.close();
    }
    channels.clear();
    groupContext.close();
  }

}
