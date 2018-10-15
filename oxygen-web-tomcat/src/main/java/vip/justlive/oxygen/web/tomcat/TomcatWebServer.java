package vip.justlive.oxygen.web.tomcat;

import java.io.File;
import java.util.concurrent.Executors;
import org.apache.catalina.Engine;
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
public class TomcatWebServer implements WebServer {

  private Tomcat tomcat;

  private void initServer() {
    if (tomcat == null) {
      tomcat = new Tomcat();
    }

    WebConf webConf = ConfigFactory.load(WebConf.class);
    String baseDir = new File(ConfigFactory.load(CoreConf.class).getBaseTempDir(),
        Tomcat.class.getSimpleName()).getAbsolutePath();
    String docBase = new File(webConf.getDocBase()).getAbsolutePath();
    tomcat.setBaseDir(baseDir);
    tomcat.getHost().setAutoDeploy(false);
    tomcat.addWebapp(webConf.getContextPath(), docBase);
    tomcat.setPort(webConf.getPort());

    TomcatConf tomcatConf = ConfigFactory.load(TomcatConf.class);
    configConnector(tomcat.getConnector(), tomcatConf);
    configEngine(tomcat.getEngine(), tomcatConf);
  }

  private void configConnector(Connector connector, TomcatConf tomcatConf) {
    connector.setURIEncoding(tomcatConf.getUriEncoding());
    ProtocolHandler protocolHandler = connector.getProtocolHandler();
    if (protocolHandler instanceof AbstractProtocol) {
      AbstractProtocol<?> handler = (AbstractProtocol) protocolHandler;
      handler.setAcceptCount(tomcatConf.getAcceptCount());
      handler.setMaxConnections(tomcatConf.getMaxConnections());
      handler.setMinSpareThreads(tomcatConf.getMinSpareThreads());
      handler.setMaxThreads(tomcatConf.getMaxThreads());
      handler.setConnectionTimeout(tomcatConf.getConnectionTimeout());
      if (handler instanceof AbstractHttp11Protocol) {
        AbstractHttp11Protocol protocol = (AbstractHttp11Protocol) handler;
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
  public synchronized void start() {
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
