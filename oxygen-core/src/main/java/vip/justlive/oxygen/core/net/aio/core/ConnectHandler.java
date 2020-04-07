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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CompletionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * aio客户端连接处理
 *
 * @author wubo
 */
@Slf4j
public class ConnectHandler implements CompletionHandler<Void, ChannelContext> {

  static final ConnectHandler INSTANCE = new ConnectHandler();

  @Override
  public void completed(Void result, ChannelContext channelContext) {
    ByteBuffer buffer = ByteBuffer.allocate(channelContext.getGroupContext().getBufferCapacity());
    buffer.order(ByteOrder.BIG_ENDIAN);
    channelContext.getChannel().read(buffer, buffer, channelContext.getReadHandler());
    channelContext.start();

    if (channelContext.getGroupContext().getAioListener() != null) {
      channelContext.getGroupContext().getAioListener().onConnected(channelContext);
    }
    channelContext.complete();
  }

  @Override
  public void failed(Throwable exc, ChannelContext channelContext) {
    log.error("Aio client {} connected error", channelContext, exc);
    channelContext.completeExceptionally(exc);
    channelContext.close();
  }
}
