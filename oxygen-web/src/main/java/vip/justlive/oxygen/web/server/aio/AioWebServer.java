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

import java.io.IOException;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.net.aio.core.GroupContext;
import vip.justlive.oxygen.core.net.aio.core.Server;
import vip.justlive.oxygen.web.WebConf;
import vip.justlive.oxygen.web.server.WebServer;

/**
 * aio web server
 *
 * @author wubo
 */
@Slf4j
public class AioWebServer implements WebServer {

  private int port;
  private Server server;

  @Override
  public void listen(int port) {
    this.port = port;
    AioServerConf serverConf = ConfigFactory.load(AioServerConf.class);
    WebConf webConf = ConfigFactory.load(WebConf.class);
    GroupContext groupContext = new GroupContext(
        new HttpServerAioHandler(webConf.getContextPath()));
    groupContext.setAioListener(new HttpServerAioListener(serverConf));
    groupContext.setAcceptThreads(serverConf.getAcceptThreads());
    groupContext.setAcceptMaxWaiter(serverConf.getAcceptMaxWaiter());
    groupContext.setWorkerThreads(serverConf.getWorkerThreads());
    groupContext.setWorkerMaxWaiter(serverConf.getWorkerMaxWaiter());
    groupContext.setDaemon(serverConf.isDaemon());
    this.server = new Server(groupContext);
    Bootstrap.start();
    try {
      this.server.start(new InetSocketAddress(this.port));
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
    log.info("aio-web-server started and listened on port [{}] with context path [{}]", this.port,
        webConf.getContextPath());
  }

  @Override
  public void stop() {
    if (server != null) {
      server.stop();
    }
  }

  @Override
  public int getPort() {
    return port;
  }
}
