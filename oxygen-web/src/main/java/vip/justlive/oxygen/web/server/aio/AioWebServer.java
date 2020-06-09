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
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.net.aio.GroupContext;
import vip.justlive.oxygen.core.util.net.aio.Server;
import vip.justlive.oxygen.web.WebConfigKeys;
import vip.justlive.oxygen.web.server.WebServer;

/**
 * aio web server
 *
 * @author wubo
 */
public class AioWebServer implements WebServer {

  private int port;
  private Server server;

  @Override
  public void listen(int port) {
    this.port = port;
    AioServerConf serverConf = ConfigFactory.load(AioServerConf.class);
    String contextPath = WebConfigKeys.SERVER_CONTEXT_PATH.getValue();
    GroupContext groupContext = new GroupContext(new HttpServerAioHandler(contextPath));
    groupContext.setAioListener(new HttpServerAioListener(serverConf));
    groupContext.setAcceptThreads(serverConf.getAcceptThreads());
    groupContext.setAcceptMaxWaiter(serverConf.getAcceptMaxWaiter());
    groupContext.setWorkerThreads(serverConf.getWorkerThreads());
    groupContext.setWorkerMaxWaiter(serverConf.getWorkerMaxWaiter());
    groupContext.setDaemon(serverConf.isDaemon());
    this.server = new Server(groupContext);
    try {
      this.server.start(new InetSocketAddress(this.port));
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
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
