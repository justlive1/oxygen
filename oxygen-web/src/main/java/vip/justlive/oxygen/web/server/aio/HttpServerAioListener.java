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

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;
import vip.justlive.oxygen.core.util.net.aio.AioListener;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.oxygen.web.http.Response;

/**
 * http aio监听
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class HttpServerAioListener implements AioListener {

  private final AioServerConf conf;

  @Override
  public void onWriteHandled(ChannelContext channelContext, Object data, Throwable throwable) {
    Response response = (Response) data;
    if (throwable != null || !HttpHeaders.CONNECTION_KEEP_ALIVE
        .equalsIgnoreCase(response.getRequest().getHeader(HttpHeaders.CONNECTION))) {
      channelContext.close();
    }
  }

  @Override
  public void onConnected(ChannelContext channelContext) {
    if (log.isDebugEnabled()) {
      log.debug("channel connected {}", channelContext);
    }
    ConnectionTimeout timeout = new ConnectionTimeout(channelContext, conf.getIdleTimeout(),
        conf.getRequestTimeout());
    ThreadUtils.globalTimer()
        .scheduleWithDelay(timeout, conf.getIdleTimeout(), TimeUnit.MILLISECONDS, timeout);
  }

  @Override
  public void onClosed(ChannelContext channelContext) {
    if (log.isDebugEnabled()) {
      log.debug("channel closed {}", channelContext);
    }
  }
}
