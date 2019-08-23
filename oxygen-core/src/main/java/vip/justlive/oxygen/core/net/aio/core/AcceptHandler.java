/*
 * Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */

package vip.justlive.oxygen.core.net.aio.core;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * aio服务端连接接收处理
 *
 * @author wubo
 */
@Slf4j
public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Server> {

  @Override
  public void completed(AsynchronousSocketChannel channel, Server server) {
    if (!channel.isOpen()) {
      return;
    }
    try {
      channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
      channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
      ByteBuffer buffer = ByteBuffer.allocate(server.getGroupContext().getBufferCapacity());
      buffer.order(ByteOrder.BIG_ENDIAN);
      ChannelContext channelContext = new ChannelContext(server.getGroupContext(), channel);
      channel.read(buffer, buffer, channelContext.getReadHandler());
      channelContext.start();

      if (server.getGroupContext().getAioListener() != null) {
        server.getGroupContext().getAioListener().onConnected(channelContext);
      }
    } catch (IOException e) {
      log.error("Aio连接处理失败", e);
    } finally {
      server.getServerChannel().accept(server, this);
    }
  }

  @Override
  public void failed(Throwable exc, Server server) {
    log.error("Aio连接接收失败", exc);
    server.getServerChannel().accept(server, this);
  }
}
