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

    boolean hasNext = true;
    try {
      if (client.getChannelContext() == null || !client.getChannelContext().isClosed()) {
        return;
      }

      int retryAttempts = client.getChannelContext().getRetryAttempts() + 1;
      client.getChannelContext().setRetryAttempts(retryAttempts);

      if (client.getGroupContext().getRetryMaxAttempts() > 0 && retryAttempts > client
          .getGroupContext().getRetryMaxAttempts()) {
        hasNext = false;
        log.error("{}客户端重连->{}超过最大次数{}", client.getChannelContext(),
            client.getGroupContext().getServerAddress(), retryAttempts);
        return;
      }

      if (log.isDebugEnabled()) {
        log.info("{}客户端开始第{}次重连->{}", client.getChannelContext(), retryAttempts,
            client.getGroupContext().getServerAddress());
      }

      ChannelContext channelContext = client.getChannelContext();
      AsynchronousSocketChannel channel = Utils.create(channelContext.getGroupContext());
      channelContext.setChannel(channel);
      channel.connect(channelContext.getGroupContext().getServerAddress(), channelContext,
          ConnectHandler.INSTANCE);
    } catch (IOException e) {
      log.error("{}客户端第{}次重连->{}失败", client.getChannelContext(),
          client.getChannelContext().getRetryAttempts(),
          client.getGroupContext().getServerAddress(), e);
    } finally {
      if (hasNext) {
        client.getGroupContext().getScheduledExecutor()
            .schedule(this, client.getGroupContext().getRetryInterval(), TimeUnit.MILLISECONDS);
      }
    }
  }
}
