/*
 * Copyright (C) 2019 justlive1
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
package vip.justlive.oxygen.web.jetty;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.PathMatcher;
import vip.justlive.oxygen.core.util.ResourceUtils;
import vip.justlive.oxygen.core.util.ResourceUtils.JarFileInfo;
import vip.justlive.oxygen.web.WebConf;
import vip.justlive.oxygen.web.server.WebServer;

/**
 * jetty web server
 *
 * @author wubo
 */
@Slf4j
public class JettyWebServer implements WebServer {

  private int port;
  private Server server;

  private PathMatcher matcher = new PathMatcher();

  @Override
  public void stop() {
    if (server == null) {
      return;
    }
    try {
      server.stop();
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public int getPort() {
    return port;
  }

  private void configConnector(ServerConnector connector, JettyConf jettyConf) {
    connector.setPort(this.port);
    connector.setIdleTimeout(jettyConf.getIdleTimeout());
    connector.setAcceptQueueSize(jettyConf.getAcceptQueueSize());
    connector.setStopTimeout(jettyConf.getStopTimeout());
    connector.setReuseAddress(jettyConf.isReuseAddress());
  }

  @Override
  public void listen(int port) {
    WebConf webConf = ConfigFactory.load(WebConf.class);
    JettyConf jettyConf = ConfigFactory.load(JettyConf.class);
    this.port = port;

    server = new Server();

    ServerConnector connector = new ServerConnector(server);
    server.addConnector(connector);
    configConnector(connector, jettyConf);

    WebAppContext webapp = new WebAppContext();
    server.setHandler(webapp);
    try {
      configWebapp(webapp, jettyConf, webConf);
      server.start();
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
    log.info("jetty started and listened on port [{}] with context path [{}]", this.port,
        webConf.getContextPath());
  }

  private void configWebapp(WebAppContext webapp, JettyConf jettyConf, WebConf webConf)
      throws URISyntaxException, IOException {
    webapp.setContextPath(webConf.getContextPath());
    webapp.setVirtualHosts(jettyConf.getVirtualHosts());
    webapp.setMaxFormContentSize(jettyConf.getMaxFormContentSize());
    webapp.setMaxFormKeys(jettyConf.getMaxFormKeys());
    webapp.setParentLoaderPriority(true);
    webapp.setConfigurationDiscovered(jettyConf.isConfigurationDiscovered());
    CoreConf conf = ConfigFactory.load(CoreConf.class);
    webapp.setTempDirectory(new File(conf.getBaseTempDir(), "jetty-temp"));
    URL url = getClass().getResource(Constants.ROOT_PATH);
    if (url != null) {
      webapp.setResourceBase(url.toURI().toASCIIString());
    } else {
      // run in jar
      File dir = new File(conf.getBaseTempDir(), "jetty-jsp");
      webapp.setResourceBase(dir.getAbsolutePath());
      if (dir.exists() || dir.mkdirs()) {
        copyJspFiles(dir, webConf);
      }
    }
    webapp.addServlet(new ServletHolder("jsp", JettyJspServlet.class), "*.jsp");
    webapp.setConfigurations(new Configuration[]{new AnnotationConfiguration()});
  }

  private void copyJspFiles(File dir, WebConf webConf) throws IOException {
    String location = ResourceUtils.concat(webConf.getJspViewPrefix(), "/**/*.jsp");
    String rootPath = matcher.getRootDir(location);
    String subPattern = location.substring(rootPath.length());
    Enumeration<URL> urls = ClassUtils.getDefaultClassLoader()
        .getResources(ResourceUtils.cutRootPath(rootPath));
    while (urls.hasMoreElements()) {
      URL rootUrl = urls.nextElement();
      this.findJarMatchPath(rootUrl, subPattern, dir);
    }
  }

  private void findJarMatchPath(URL rootUrl, String subPattern, File dir) throws IOException {
    JarFileInfo jarFileInfo = ResourceUtils.getJarFileInfo(rootUrl);
    try {
      if (log.isDebugEnabled()) {
        log.debug("Looking for jsp resources in jar file [" + jarFileInfo.jarFileUrl + "]");
      }
      String rootEntryPath = jarFileInfo.rootEntryPath;
      if (rootEntryPath.length() > 0 && !rootEntryPath.endsWith(Constants.PATH_SEPARATOR)) {
        rootEntryPath += Constants.PATH_SEPARATOR;
      }
      for (Enumeration<JarEntry> entries = jarFileInfo.jarFile.entries();
          entries.hasMoreElements(); ) {
        JarEntry entry = entries.nextElement();
        String entryPath = entry.getName();
        if (entryPath.startsWith(rootEntryPath) && matcher
            .match(subPattern, entryPath.substring(rootEntryPath.length()))) {
          log.info("find jsp file [{}]", entryPath);
          File file = mkdirs(dir, entryPath);
          Files.copy(jarFileInfo.jarFile.getInputStream(entry), file.toPath());
        }
      }
    } finally {
      jarFileInfo.jarFile.close();
    }
  }

  private File mkdirs(File dir, String name) throws IOException {
    int index = name.lastIndexOf(Constants.ROOT_PATH);
    if (index > 0) {
      File newDir = new File(dir, name.substring(0, index));
      if (newDir.mkdirs()) {
        log.info("create dir [{}] for jsp [{}]", newDir, name);
      }
    }
    File file = new File(dir, name);
    if (file.exists()) {
      Files.delete(file.toPath());
    }
    return file;
  }
}
