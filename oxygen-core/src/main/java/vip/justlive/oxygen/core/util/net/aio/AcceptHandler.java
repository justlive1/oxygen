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

import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
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
    try {
      if (log.isDebugEnabled()) {
        log.debug("Aio accept {}", channel);
      }
      if (!channel.isOpen()) {
        log.warn("channel has closed {}", channel);
        return;
      }
      channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
      channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

      ChannelContext channelContext = new ChannelContext(server.getGroupContext(), channel);
      channelContext.setServerAddress(server.getServerAddress());
      channelContext.start();

      ByteBuffer buffer = ByteBuffer.allocate(server.getGroupContext().getBufferCapacity());
      buffer.order(ByteOrder.BIG_ENDIAN);
      channel.read(buffer, buffer, channelContext.getReadHandler());

      if (server.getGroupContext().getAioListener() != null) {
        server.getGroupContext().getAioListener().onConnected(channelContext);
      }
    } catch (Exception e) {
      log.error("Aio accept completed error", e);
    } finally {
      server.getServerChannel().accept(server, this);
    }
  }

  @Override
  public void failed(Throwable exc, Server server) {
    log.error("Aio accept error", exc);
    if (exc instanceof ClosedChannelException && server.getGroupContext().isStopped()) {
      return;
    }
    server.getServerChannel().accept(server, this);
  }
}
