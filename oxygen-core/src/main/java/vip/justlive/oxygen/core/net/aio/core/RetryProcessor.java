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

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端重连任务
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class RetryProcessor implements Runnable {

  private final Client client;

  @Override
  public void run() {
    if (!client.getGroupContext().isRetryEnabled() || client.getGroupContext().isStopped()) {
      return;
    }

    try {
      client.getChannels().values().forEach(this::handle);
    } finally {
      if (!client.getChannels().isEmpty()) {
        client.getGroupContext().getScheduledExecutor()
            .schedule(this, client.getGroupContext().getRetryInterval(), TimeUnit.MILLISECONDS);
      }
    }
  }

  private void handle(ChannelContext channelContext) {
    try {
      if (!channelContext.isClosed()) {
        return;
      }

      int retryAttempts = channelContext.getRetryAttempts() + 1;
      channelContext.setRetryAttempts(retryAttempts);

      if (client.getGroupContext().getRetryMaxAttempts() > 0 && retryAttempts > client
          .getGroupContext().getRetryMaxAttempts()) {
        log.error("{} client try to connect to {} reached the max attempts [{}]", channelContext,
            channelContext.getServerAddress(), client.getGroupContext().getRetryMaxAttempts());
        client.close(channelContext);
        return;
      }

      if (log.isDebugEnabled()) {
        log.info("{} client try to connect to {} for {} attempt(s)", channelContext,
            channelContext.getServerAddress(), retryAttempts);
      }

      AsynchronousSocketChannel channel = Utils.create(channelContext.getGroupContext());
      channelContext.setChannel(channel);
      channel.connect(channelContext.getServerAddress(), channelContext,
          ConnectHandler.INSTANCE);
      channelContext.join();
    } catch (IOException e) {
      log.error("{} client try to connect to {} failed for {} attempt(s)", channelContext,
          channelContext.getRetryAttempts(), channelContext.getServerAddress(), e);
    }
  }
}
