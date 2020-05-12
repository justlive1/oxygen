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

package vip.justlive.oxygen.web.tomcat;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.FileUtils;
import vip.justlive.oxygen.core.util.ThreadUtils;
import vip.justlive.oxygen.web.WebConf;
import vip.justlive.oxygen.web.server.WebServer;

/**
 * tomcat server
 *
 * @author wubo
 */
@Slf4j
public class TomcatWebServer implements WebServer {

  private Tomcat tomcat;
  private int port;

  private void initServer(WebConf webConf) {
    if (tomcat == null) {
      tomcat = new Tomcat();
    }

    File baseDir = FileUtils.createTempDir(Tomcat.class.getSimpleName());
    File docBase = new File(baseDir, Context.class.getSimpleName());
    FileUtils.mkdirs(docBase);
    tomcat.setBaseDir(baseDir.getAbsolutePath());
    Host host = tomcat.getHost();
    host.setAutoDeploy(false);
    Context ctx = tomcat.addWebapp(host, webConf.getContextPath(), docBase.getAbsolutePath(),
        new FatJarContextConfig());
    ctx.setJarScanner(new FatJarScanner());
    ctx.setParentClassLoader(getClass().getClassLoader());
    ctx.addLifecycleListener(new FatJarWebXmlListener());
    tomcat.setPort(port);

    TomcatConf tomcatConf = ConfigFactory.load(TomcatConf.class);
    configConnector(tomcat.getConnector(), tomcatConf);
    configEngine(tomcat.getEngine(), tomcatConf);
  }

  private void configConnector(Connector connector, TomcatConf tomcatConf) {
    connector.setURIEncoding(tomcatConf.getUriEncoding());
    ProtocolHandler protocolHandler = connector.getProtocolHandler();
    if (protocolHandler instanceof AbstractProtocol) {
      AbstractProtocol<?> handler = (AbstractProtocol<?>) protocolHandler;
      handler.setAcceptCount(tomcatConf.getAcceptCount());
      handler.setMaxConnections(tomcatConf.getMaxConnections());
      handler.setMinSpareThreads(tomcatConf.getMinSpareThreads());
      handler.setMaxThreads(tomcatConf.getMaxThreads());
      handler.setConnectionTimeout(tomcatConf.getConnectionTimeout());
      if (handler instanceof AbstractHttp11Protocol) {
        AbstractHttp11Protocol<?> protocol = (AbstractHttp11Protocol<?>) handler;
        protocol.setMaxHttpHeaderSize(tomcatConf.getMaxHttpHeaderSize());
      }
    }
  }

  private void configEngine(Engine engine, TomcatConf tomcatConf) {
    engine.setBackgroundProcessorDelay(tomcatConf.getBackgroundProcessorDelay());
    if (tomcatConf.isAccessLogEnabled()) {
      AccessLogValve value = new AccessLogValve();
      value.setBuffered(tomcatConf.isAccessLogBuffered());
      value.setFileDateFormat(tomcatConf.getAccessLogFileFormat());
      value.setRequestAttributesEnabled(tomcatConf.isAccessLogRequestAttributesEnabled());
      value.setPattern(tomcatConf.getAccessLogPattern());
      engine.getPipeline().addValve(value);
    }

  }

  private void startDaemonAwaitThread() {
    ThreadUtils.defaultThreadFactory().newThread(tomcat.getServer()::await).start();
  }

  @Override
  public synchronized void listen(int port) {
    this.port = port;
    WebConf conf = ConfigFactory.load(WebConf.class);
    initServer(conf);
    try {
      tomcat.start();
      log.info("tomcat started and listened on port [{}] with context path [{}]", this.port,
          conf.getContextPath());
      startDaemonAwaitThread();
    } catch (LifecycleException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public synchronized void stop() {
    if (tomcat != null) {
      try {
        tomcat.stop();
      } catch (LifecycleException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  @Override
  public int getPort() {
    return port;
  }
}
