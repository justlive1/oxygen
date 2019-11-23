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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import vip.justlive.oxygen.core.util.MoreObjects;

/**
 * 写操作worker
 *
 * @author wubo
 */
public class WriteWorker extends AbstractWorker<Object> {

  private final AioHandler aioHandler;
  private final Semaphore semaphore = new Semaphore(1);

  WriteWorker(ChannelContext channelContext) {
    super(channelContext);
    this.aioHandler = channelContext.getGroupContext().getAioHandler();
  }

  @Override
  public void stop() {
    semaphore.release();
    super.stop();
  }

  @Override
  public void handle(List<Object> data) {
    if (stopped) {
      return;
    }

    List<ByteBuffer> buffers = new ArrayList<>(data.size());
    for (Object obj : data) {
      ByteBuffer buffer = aioHandler.encode(obj, channelContext);
      if (!buffer.hasRemaining()) {
        buffer.flip();
      }
      buffers.add(buffer);
    }

    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      AioListener listener = channelContext.getGroupContext().getAioListener();
      if (listener != null) {
        MoreObjects.caughtForeach(data, item -> listener.onWriteHandled(channelContext, item, e));
      }
      Thread.currentThread().interrupt();
      return;
    }

    if (channelContext.isClosed()) {
      semaphore.release();
      return;
    }

    CompletableFuture<Void> future = new CompletableFuture<>();
    future.whenComplete((r, e) -> complete(e, data));
    try {
      WriteHandler.WriteContext ctx = new WriteHandler.WriteContext(future,
          Utils.composite(buffers));
      channelContext.getChannel().write(ctx.buffer, ctx, channelContext.getWriteHandler());
    } catch (Exception e) {
      complete(e, data);
      throw e;
    }
  }

  private void complete(Throwable exc, List<Object> data) {
    semaphore.release();
    AioListener listener = channelContext.getGroupContext().getAioListener();
    if (listener != null) {
      MoreObjects.caughtForeach(data, item -> listener.onWriteHandled(channelContext, item, exc));
    }
  }

}
