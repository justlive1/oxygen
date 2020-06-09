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

import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.function.LongUnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;
import vip.justlive.oxygen.core.util.io.IoUtils;

/**
 * 客户端重连任务
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class RetryProcessor implements Runnable, LongUnaryOperator {

  private final ChannelContext channelContext;

  @Override
  public void run() {
    try {
      if (!channelContext.isClosed()) {
        return;
      }

      int retryAttempts = channelContext.getRetryAttempts() + 1;
      channelContext.setRetryAttempts(retryAttempts);

      if (channelContext.getGroupContext().getRetryMaxAttempts() > 0
          && retryAttempts > channelContext.getGroupContext().getRetryMaxAttempts()) {
        log.error("{} client try to connect to {} reached the max attempts [{}]", channelContext,
            channelContext.getServerAddress(),
            channelContext.getGroupContext().getRetryMaxAttempts());
        channelContext.close();
        return;
      }

      if (log.isDebugEnabled()) {
        log.info("{} client try to connect to {} for {} attempt(s)", channelContext,
            channelContext.getServerAddress(), retryAttempts);
      }

      AsynchronousSocketChannel channel = IoUtils
          .create(channelContext.getGroupContext().getChannelGroup());
      channelContext.setChannel(channel);
      channel.connect(channelContext.getServerAddress(), channelContext, ConnectHandler.INSTANCE);
      channelContext.join();

      BeatProcessor beat = new BeatProcessor(channelContext);
      ThreadUtils.globalTimer()
          .scheduleWithDelay(beat, channelContext.getGroupContext().getBeatInterval(),
              TimeUnit.MILLISECONDS, beat);
    } catch (Exception e) {
      log.error("{} client try to connect to {} failed for {} attempt(s)", channelContext,
          channelContext.getRetryAttempts(), channelContext.getServerAddress(), e);
    }
  }

  @Override
  public long applyAsLong(long operand) {
    boolean stop =
        !channelContext.isClosed() || (channelContext.getGroupContext().getRetryMaxAttempts() > 0
            && channelContext.getRetryAttempts() > channelContext.getGroupContext()
            .getRetryMaxAttempts());
    if (stop) {
      return Long.MIN_VALUE;
    }
    return System.currentTimeMillis() + channelContext.getGroupContext().getRetryInterval();
  }
}
