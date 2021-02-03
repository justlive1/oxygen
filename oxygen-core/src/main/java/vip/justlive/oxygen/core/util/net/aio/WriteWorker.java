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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.CoreConfigKeys;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.concurrent.AbstractQueueWorker;
import vip.justlive.oxygen.core.util.io.IoUtils;

/**
 * 写操作worker
 *
 * @author wubo
 */
@Slf4j
public class WriteWorker extends AbstractQueueWorker<Object> {

  private final AioHandler aioHandler;
  private final Semaphore semaphore;
  private final ChannelContext channelContext;
  private final boolean useFuture;
  private CompletableFuture<Void> writeFuture;

  WriteWorker(ChannelContext channelContext) {
    super(channelContext.getGroupContext().getWorkerExecutor());
    this.channelContext = channelContext;
    this.aioHandler = channelContext.getGroupContext().getAioHandler();
    this.useFuture = CoreConfigKeys.AIO_USE_FUTURE.castValue(boolean.class);
    if (this.useFuture) {
      writeFuture = CompletableFuture.completedFuture(null);
      semaphore = null;
    } else {
      semaphore = new Semaphore(1, true);
    }
  }

  @Override
  public void stop() {
    if (this.useFuture) {
      writeFuture.cancel(true);
    }
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

    if (channelContext.isClosed()) {
      return;
    }

    ByteBuffer buffer = IoUtils.composite(buffers);
    if (this.useFuture) {
      write(buffer, data);
    } else {
      write0(buffer, data);
    }
  }

  private void write0(ByteBuffer buffer, List<Object> data) {
    semaphore.acquireUninterruptibly();
    if (channelContext.isClosed()) {
      semaphore.release();
      return;
    }

    CompletableFuture<Void> future = new CompletableFuture<>();
    future.whenComplete((r, e) -> complete(e, data));
    write(future, buffer);
  }

  private synchronized void write(ByteBuffer buffer, List<Object> data) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    future.whenComplete((r, e) -> complete(e, data));
    writeFuture.whenComplete((r, e) -> write(future, buffer));
    writeFuture = future;
  }

  private void write(CompletableFuture<Void> future, ByteBuffer buffer) {
    try {
      WriteHandler.WriteContext ctx = new WriteHandler.WriteContext(future, buffer);
      channelContext.getChannel().write(ctx.buffer, ctx, channelContext.getWriteHandler());
    } catch (Exception e) {
      log.error("{} write error", channelContext, e);
      future.completeExceptionally(e);
    }
  }

  private void complete(Throwable exc, List<Object> data) {
    if (!useFuture) {
      semaphore.release();
    }
    AioListener listener = channelContext.getGroupContext().getAioListener();
    if (listener != null) {
      MoreObjects.caughtForeach(data, item -> listener.onWriteHandled(channelContext, item, exc));
    }
  }

}
