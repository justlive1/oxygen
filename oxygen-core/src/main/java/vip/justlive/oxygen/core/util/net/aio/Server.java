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
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * aio服务端
 *
 * @author wubo
 */
@Getter
@RequiredArgsConstructor
public class Server {

  private final GroupContext groupContext;
  private InetSocketAddress serverAddress;
  private AsynchronousServerSocketChannel serverChannel;

  /**
   * 启动服务
   *
   * @param host 主机
   * @param port 端口
   * @throws IOException io异常时抛出
   */
  public void start(String host, int port) throws IOException {
    start(new InetSocketAddress(host, port));
  }

  /**
   * 启动服务
   *
   * @param address 地址
   * @throws IOException io异常时抛出
   */
  @SuppressWarnings("squid:S2095")
  public void start(InetSocketAddress address) throws IOException {
    groupContext.setStopped(false);
    AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup
        .withThreadPool(groupContext.getGroupExecutor());
    groupContext.setChannelGroup(channelGroup);
    serverAddress = address;
    serverChannel = AsynchronousServerSocketChannel.open(channelGroup)
        .setOption(StandardSocketOptions.SO_REUSEADDR, true).bind(address);
    serverChannel.accept(this, new AcceptHandler());
  }

  /**
   * 停止服务
   */
  public void stop() {
    if (groupContext.isStopped()) {
      return;
    }
    groupContext.setStopped(true);
    try {
      serverChannel.close();
    } catch (IOException e) {
      // ignore
    }
    groupContext.close();
  }

}
