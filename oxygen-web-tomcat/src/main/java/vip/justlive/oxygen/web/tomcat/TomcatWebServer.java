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
import java.util.Collections;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.coyote.http2.Http2Protocol;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;
import vip.justlive.oxygen.core.util.io.FileUtils;
import vip.justlive.oxygen.web.WebConfigKeys;
import vip.justlive.oxygen.web.server.WebServer;
import vip.justlive.oxygen.web.servlet.ServletWebInitializer;

/**
 * tomcat server
 *
 * @author wubo
 */
public class TomcatWebServer implements WebServer {

  private Tomcat tomcat;
  private int port;
  private String contextPath;

  private void initServer() {
    if (tomcat == null) {
      tomcat = new Tomcat();
    }

    File baseDir = FileUtils.createTempDir(Tomcat.class.getSimpleName());
    File docBase = new File(baseDir, Context.class.getSimpleName());
    FileUtils.mkdirs(docBase);
    tomcat.setBaseDir(baseDir.getAbsolutePath());

    if (AprLifecycleListener.isAprAvailable()) {
      tomcat.getServer().addLifecycleListener(new AprLifecycleListener());
    }

    TomcatConf tomcatConf = ConfigFactory.load(TomcatConf.class);

    Connector connector = new Connector();
    connector.setThrowOnFailure(true);
    tomcat.getService().addConnector(connector);

    connector.setPort(port);
    configConnector(tomcat.getConnector(), tomcatConf);
    tomcat.setConnector(connector);

    Host host = tomcat.getHost();
    host.setAutoDeploy(false);

    configEngine(tomcat.getEngine(), tomcatConf);

    Context ctx = tomcat.addWebapp(host, contextPath, docBase.getAbsolutePath(),
        new FatJarContextConfig());
    ctx.setJarScanner(new FatJarScanner());
    ctx.setParentClassLoader(getClass().getClassLoader());
    ctx.addLifecycleListener(new FatJarWebXmlListener());
    ctx.addServletContainerInitializer(new ServletWebInitializer(), Collections.emptySet());
    tomcat.setPort(port);


  }

  private void configConnector(Connector connector, TomcatConf tomcatConf) {
    connector.setURIEncoding(tomcatConf.getUriEncoding());
    if (tomcatConf.isHttp2Enabled()) {
      connector.addUpgradeProtocol(new Http2Protocol());
    }
    ProtocolHandler protocolHandler = connector.getProtocolHandler();
    if (protocolHandler instanceof AbstractProtocol<?> handler) {
      handler.setAcceptCount(tomcatConf.getAcceptCount());
      handler.setMaxConnections(tomcatConf.getMaxConnections());
      handler.setMinSpareThreads(tomcatConf.getMinSpareThreads());
      handler.setMaxThreads(tomcatConf.getMaxThreads());
      handler.setConnectionTimeout(tomcatConf.getConnectionTimeout());
      if (handler instanceof AbstractHttp11Protocol<?> protocol) {
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
    this.contextPath = WebConfigKeys.SERVER_CONTEXT_PATH.getValue();
    initServer();
    try {
      tomcat.start();
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
