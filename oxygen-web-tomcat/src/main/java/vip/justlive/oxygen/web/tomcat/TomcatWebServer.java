package vip.justlive.oxygen.web.tomcat;

import java.io.File;
import java.util.concurrent.Executors;
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
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.exception.Exceptions;
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

  private void checkDir(File... dirs) {
    if (dirs == null) {
      return;
    }
    for (File dir : dirs) {
      if (!dir.exists() && dir.mkdirs()) {
        log.info("create dir [{}]", dir.getAbsolutePath());
      }
    }
  }

  private void initServer() {
    if (tomcat == null) {
      tomcat = new Tomcat();
    }

    WebConf webConf = ConfigFactory.load(WebConf.class);
    File baseDir = new File(ConfigFactory.load(CoreConf.class).getBaseTempDir(),
        Tomcat.class.getSimpleName());
    File docBase = new File(baseDir, Context.class.getSimpleName());
    checkDir(baseDir, docBase);
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
    Executors.defaultThreadFactory().newThread(tomcat.getServer()::await).start();
  }

  @Override
  public synchronized void listen(int port) {
    this.port = port > 0 ? port : ConfigFactory.load(WebConf.class).getPort();
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
    if (tomcat != null && tomcat.getConnector() != null) {
      return tomcat.getConnector().getPort();
    }
    return 0;
  }
}
