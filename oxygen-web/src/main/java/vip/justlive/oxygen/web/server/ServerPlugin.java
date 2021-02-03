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
package vip.justlive.oxygen.web.server;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.util.base.ServiceLoaderUtils;
import vip.justlive.oxygen.web.WebConfigKeys;
import vip.justlive.oxygen.web.server.aio.AioWebServer;

/**
 * web服务插件
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class ServerPlugin implements Plugin {

  @Getter
  @NonNull
  private int port;
  WebServer webServer;

  @Override
  public int order() {
    return Integer.MIN_VALUE + 200;
  }

  @Override
  public void start() {
    webServer = ServiceLoaderUtils.loadServiceOrNull(WebServer.class);
    if (webServer == null) {
      webServer = new AioWebServer();
    }
    port = WebConfigKeys.SERVER_PORT.castValue(Integer.class, port);
    webServer.listen(port);
  }

  @Override
  public void stop() {
    webServer.stop();
  }
}
