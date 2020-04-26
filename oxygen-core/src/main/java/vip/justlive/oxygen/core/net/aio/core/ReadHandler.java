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

package vip.justlive.oxygen.core.net.aio.core;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CompletionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * aio 读取操作处理
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

  private final ChannelContext channelContext;

  @Override
  public void completed(Integer result, ByteBuffer buffer) {
    if (result <= 0) {
      if (result == -1) {
        log.info("{} target closed", channelContext);
      } else {
        log.warn("{} read result {}", channelContext, result);
      }
      channelContext.close();
      return;
    }

    buffer.flip();
    channelContext.read(buffer);

    if (!channelContext.isClosed()) {
      buffer.clear();
      channelContext.getChannel().read(buffer, buffer, this);
    }
  }

  @Override
  public void failed(Throwable exc, ByteBuffer buffer) {
    if (!(channelContext.isClosed() && exc instanceof AsynchronousCloseException)) {
      log.error("{} read error", channelContext, exc);
    }
    channelContext.close();
  }
}
