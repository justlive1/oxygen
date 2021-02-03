/*
 * Copyright (C) 2020 the original author or authors.
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

package vip.justlive.oxygen.core.util.net.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import vip.justlive.oxygen.core.util.base.SystemUtils;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;
import vip.justlive.oxygen.core.util.io.IoUtils;

/**
 * aio 客户端
 *
 * @author wubo
 */
public class Client {

  @Getter
  private final GroupContext groupContext;

  public Client(GroupContext groupContext) {
    this.groupContext = groupContext;
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
    AsynchronousSocketChannel channel = IoUtils.create(channelGroup, bind);
    groupContext.setAioListener(new ComposeAioListener().add(ClientAioListener.INSTANCE)
        .add(groupContext.getAioListener()));

    ChannelContext channelContext = new ChannelContext(groupContext, channel, false);
    channelContext.setServerAddress(remote);
    channel.connect(remote, channelContext, ConnectHandler.INSTANCE);

    BeatProcessor beat = new BeatProcessor(channelContext);
    ThreadUtils.globalTimer()
        .scheduleWithDelay(beat, groupContext.getBeatInterval(), TimeUnit.MILLISECONDS, beat);
    return channelContext;
  }

  /**
   * 关闭客户端
   */
  public void close() {
    groupContext.close();
  }

}
