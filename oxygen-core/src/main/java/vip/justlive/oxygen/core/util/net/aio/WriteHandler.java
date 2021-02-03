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

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * aio写操作处理
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class WriteHandler implements CompletionHandler<Integer, WriteHandler.WriteContext> {

  private final ChannelContext channelContext;

  @Override
  public void completed(Integer result, WriteContext ctx) {
    channelContext.setLastSentAt(System.currentTimeMillis());
    if (ctx.buffer.hasRemaining()) {
      if (log.isDebugEnabled()) {
        log.debug("{} sent remained. {}/{}", channelContext, ctx.buffer.position(),
            ctx.buffer.limit());
      }
      channelContext.getChannel().write(ctx.buffer, ctx, this);
    } else {
      ctx.future.complete(null);
    }
  }

  @Override
  public void failed(Throwable exc, WriteContext ctx) {
    ctx.future.completeExceptionally(exc);
  }

  @RequiredArgsConstructor
  static class WriteContext {

    final CompletableFuture<Void> future;
    final ByteBuffer buffer;

  }
}
