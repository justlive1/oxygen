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
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 读操作worker
 *
 * @author wubo
 */
@Slf4j
public class ReadWorker extends AbstractWorker<ByteBuffer> {

  private final AioHandler aioHandler;
  /**
   * 上一次遗留buffer
   */
  private ByteBuffer lastByteBuffer = null;

  ReadWorker(ChannelContext channelContext) {
    super(channelContext);
    this.aioHandler = channelContext.getGroupContext().getAioHandler();
  }

  @Override
  public void handle(List<ByteBuffer> buffers) {
    if (lastByteBuffer != null) {
      buffers.add(0, lastByteBuffer);
      lastByteBuffer = null;
    }
    ByteBuffer buffer = Utils.composite(buffers);
    while (buffer.hasRemaining()) {
      try {
        int position = buffer.position();
        int limit = buffer.limit();
        int readableSize = limit - position;
        Object data = aioHandler.decode(buffer, readableSize, channelContext);
        if (data != null) {
          //解码成功
          if (log.isDebugEnabled()) {
            log.debug("{} decoded packet successfully,{} bytes", channelContext,
                buffer.position() - position);
          }
          channelContext.setLastReceivedAt(System.currentTimeMillis());
          doHandle(data);
        } else {
          //数据不够
          buffer.position(position);
          buffer.limit(limit);
          lastByteBuffer = buffer;
          if (log.isDebugEnabled()) {
            log.debug("{} decoded failed,remain {} bytes", channelContext, readableSize);
          }
          break;
        }
      } catch (Exception e) {
        log.error("aio decode error", e);
        channelContext.close();
        return;
      }
    }
  }

  private void doHandle(Object data) {
    Throwable e = null;
    try {
      aioHandler.handle(data, channelContext);
    } catch (Throwable exc) {
      e = exc;
    } finally {
      this.afterHandled(data, e);
    }
  }

  private void afterHandled(Object data, Throwable e) {
    try {
      if (channelContext.getGroupContext().getAioListener() != null) {
        channelContext.getGroupContext().getAioListener().onReadHandled(channelContext, data, e);
      }
    } catch (Exception exc) {
      log.error("{} handled listener error", channelContext, exc);
    }
  }
}
