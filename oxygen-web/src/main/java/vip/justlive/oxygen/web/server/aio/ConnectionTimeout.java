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

package vip.justlive.oxygen.web.server.aio;

import java.util.function.LongUnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.net.aio.core.ChannelContext;

/**
 * 空闲连接处理
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class ConnectionTimeout implements Runnable, LongUnaryOperator {

  private final ChannelContext channelContext;
  private final long idleTimeout;
  private final long requestTimeout;

  @Override
  public void run() {
    long now = System.currentTimeMillis();
    long last = Math
        .max(Math.max(channelContext.getLastReceivedAt(), channelContext.getLastSentAt()),
            channelContext.getCreateAt());
    boolean isIdle = (channelContext.getLastReceivedAt() < channelContext.getLastSentAt() || (
        channelContext.getLastSentAt() < 0 && channelContext.getLastReceivedAt() < 0))
        && last + idleTimeout < now;
    if (isIdle) {
      if (log.isDebugEnabled()) {
        log.debug("Timing out idle connection from {}", channelContext);
      }
      channelContext.close();
      return;
    }

    if (requestTimeout > 0 && channelContext.getLastReceivedAt() > channelContext.getLastSentAt()
        && last + requestTimeout < now) {
      if (log.isDebugEnabled()) {
        log.debug("Closing channel because of request timeout from {}", channelContext);
      }
      channelContext.close();
    }
  }

  @Override
  public long applyAsLong(long deadline) {
    if (channelContext.isClosed()) {
      return Long.MIN_VALUE;
    }
    return deadline + idleTimeout;
  }
}
