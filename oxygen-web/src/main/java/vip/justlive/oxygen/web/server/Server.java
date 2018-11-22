/*
 * Copyright (C) 2018 justlive1
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

import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Bootstrap;

/**
 * server 启动类
 *
 * @author wubo
 */
@Slf4j
public class Server {

  private static WebServer webServer;

  Server() {
  }

  static {
    ServiceLoader<WebServer> loader = ServiceLoader.load(WebServer.class);
    if (loader.iterator().hasNext()) {
      webServer = loader.iterator().next();
    }
    Bootstrap.initConfig();
  }

  /**
   * 启动web server
   */
  public static void start() {
    if (webServer == null) {
      throw new IllegalStateException("No WebServer provided");
    }
    log.info("start web server ...");
    webServer.start();
    log.info("started web server on port [{}] !", webServer.getPort());
  }

  /**
   * 关闭web server
   */
  public static void stop() {
    log.info("stop web server ...");
    if (webServer != null) {
      webServer.stop();
    }
  }

}
