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

import java.util.function.LongUnaryOperator;
import lombok.AllArgsConstructor;

/**
 * 心跳任务
 *
 * @author wubo
 */
@AllArgsConstructor
public class BeatProcessor implements Runnable, LongUnaryOperator {

  private final ChannelContext channelContext;

  @Override
  public void run() {
    if (channelContext.isClosed()) {
      return;
    }
    long last = Math.max(channelContext.getLastReceivedAt(), channelContext.getLastSentAt());
    if (System.currentTimeMillis() - last >= channelContext.getGroupContext().getBeatInterval()) {
      Object beat = channelContext.getGroupContext().getAioHandler().beat(channelContext);
      if (beat == null) {
        return;
      }
      channelContext.write(beat);
    }
  }

  @Override
  public long applyAsLong(long deadline) {
    if (channelContext.getGroupContext().isStopped()
        || channelContext.getGroupContext().getAioHandler().beat(channelContext) == null) {
      return Long.MIN_VALUE;
    }
    long nextTime = Math.max(channelContext.getLastReceivedAt(), channelContext.getLastSentAt())
        + channelContext.getGroupContext().getBeatInterval();
    if (nextTime <= deadline) {
      nextTime = deadline + channelContext.getGroupContext().getBeatInterval();
    }
    return nextTime;
  }
}
