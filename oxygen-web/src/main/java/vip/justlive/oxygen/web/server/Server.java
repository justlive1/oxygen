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

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.web.WebConfigKeys;

/**
 * server 启动类
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class Server {

  private volatile boolean ready = false;

  /**
   * 监听端口并启动服务
   */
  public void listen() {
    listen(8080);
  }

  /**
   * 监听端口并启动服务
   *
   * @param port 端口
   */
  public void listen(int port) {
    if (!ready) {
      ready = true;
      ServerPlugin plugin = new ServerPlugin(port);
      Bootstrap.addCustomPlugin(plugin);
      Bootstrap.start();
      log.info("[{}] started and listened on port [{}] with context path [{}]",
          plugin.webServer.getClass().getSimpleName(), plugin.getPort(),
          WebConfigKeys.SERVER_CONTEXT_PATH.getValue());
    }
  }

  /**
   * 关闭服务
   */
  public void stop() {
    if (ready) {
      Bootstrap.close();
    }
  }

}
