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

import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.util.ServiceLoaderUtils;

/**
 * server 启动类
 *
 * @author wubo
 */
public class Server {

  private WebServer webServer;

  private volatile boolean ready = false;

  /**
   * web server
   *
   * @return webServer
   */
  public static Server server() {
    Server server = new Server();
    server.webServer = ServiceLoaderUtils.loadSerivce(WebServer.class);
    return server;
  }

  /**
   * 监听端口并启动服务
   */
  public void listen() {
    listen(-1);
  }

  /**
   * 监听端口并启动服务
   *
   * @param port 端口
   */
  public void listen(int port) {
    if (!ready) {
      ready = true;
      Bootstrap.initConfig();
      webServer.listen(port);
    }
  }

  /**
   * 关闭服务
   */
  public void stop() {
    if (ready) {
      ready = false;
      webServer.stop();
    }
  }

}
