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
package vip.justlive.oxygen.web.jetty;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.ee10.jsp.JettyJspServlet;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.ServletMapping;
import org.eclipse.jetty.ee10.webapp.Configuration;
import org.eclipse.jetty.ee10.webapp.JspConfiguration;
import org.eclipse.jetty.ee10.webapp.WebAppConfiguration;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.JarFileInfo;
import vip.justlive.oxygen.core.util.base.PathMatcher;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.base.Urls;
import vip.justlive.oxygen.core.util.io.FileUtils;
import vip.justlive.oxygen.web.WebConfigKeys;
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
  private String contextPath;

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
    connector.setReuseAddress(jettyConf.isReuseAddress());
  }

  @Override
  public void listen(int port) {
    JettyConf jettyConf = ConfigFactory.load(JettyConf.class);
    this.port = port;
    this.contextPath = WebConfigKeys.SERVER_CONTEXT_PATH.getValue();

    server = new Server();

    HttpConfiguration httpConfiguration = new HttpConfiguration();
    httpConfiguration.setSendServerVersion(false);
    List<ConnectionFactory> connectionFactories = new ArrayList<>();
    connectionFactories.add(new HttpConnectionFactory(httpConfiguration));
//    if (jettyConf.isHttp2Enabled()) {
//
//    }
    ServerConnector connector = new ServerConnector(server, jettyConf.getThreadsAcceptors(),
        jettyConf.getThreadsSelectors(), connectionFactories.toArray(new ConnectionFactory[0]));
    configConnector(connector, jettyConf);

    server.addConnector(connector);
    server.setStopTimeout(jettyConf.getStopTimeout());

    WebAppContext webapp = new WebAppContext();
    webapp.getContext().setExtendedListenerTypes(true);
    server.setHandler(webapp);
    try {
      configWebapp(webapp, jettyConf);
      server.start();
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  private void configWebapp(WebAppContext webapp, JettyConf jettyConf)
      throws URISyntaxException, IOException {
    webapp.setContextPath(Strings.hasText(contextPath) ? contextPath : "/");

    webapp.setVirtualHosts(jettyConf.getVirtualHosts());
    webapp.setMaxFormContentSize(jettyConf.getMaxFormContentSize());
    webapp.setMaxFormKeys(jettyConf.getMaxFormKeys());
    webapp.setParentLoaderPriority(true);
    webapp.setConfigurationDiscovered(jettyConf.isConfigurationDiscovered());
    webapp.setTempDirectory(new File(FileUtils.tempBaseDir(), "jetty-temp"));

    URL url = getClass().getResource(Strings.SLASH);
    if (url != null) {
      webapp.setBaseResourceAsString(url.toURI().toASCIIString());
    } else {
      // run in jar
      File dir = new File(FileUtils.tempBaseDir(), "jetty-jsp");
      webapp.setBaseResourceAsPath(dir.toPath());
      if (dir.exists() || dir.mkdirs()) {
        copyJspFiles(dir);
      }
    }

    addJspServlet(webapp);
    webapp.setConfigurations(new Configuration[]{new JspConfiguration(), new WebAppConfiguration(),
        new JettyServletInitializerConfiguration()

    });
    webapp.setThrowUnavailableOnStartupException(true);
  }

  private void addJspServlet(WebAppContext webapp) {
    ServletHolder holder = new ServletHolder("jsp", JettyJspServlet.class);
    holder.setInitParameter("fork", "false");
    holder.setInitParameter("development", "false");
    holder.setInitOrder(3);
    webapp.getServletHandler().addServlet(holder);
    ServletMapping mapping = new ServletMapping();
    mapping.setServletName("jsp");
    mapping.setPathSpecs(new String[]{"*.jsp", "*.jspx"});
    webapp.getServletHandler().addServletMapping(mapping);
  }

  private void copyJspFiles(File dir) throws IOException {
    String location = Urls.concat(WebConfigKeys.VIEW_PREFIX_JSP.getValue(), "/**/*.jsp");
    String rootPath = PathMatcher.getRootDir(location);
    String subPattern = location.substring(rootPath.length());
    Enumeration<URL> urls = ClassUtils.getDefaultClassLoader()
        .getResources(Urls.cutRootPath(rootPath));
    while (urls.hasMoreElements()) {
      URL rootUrl = urls.nextElement();
      this.findJarMatchPath(rootUrl, subPattern, dir);
    }
  }

  private void findJarMatchPath(URL rootUrl, String subPattern, File dir) throws IOException {
    try (JarFileInfo jarFileInfo = Urls.getJarFileInfo(rootUrl)) {
      if (log.isDebugEnabled()) {
        log.debug("Looking for jsp resources in jar file [{}]", jarFileInfo.jarFileUrl);
      }
      String rootEntryPath = jarFileInfo.rootEntryPath;
      if (!rootEntryPath.isEmpty() && !rootEntryPath.endsWith(Strings.SLASH)) {
        rootEntryPath += Strings.SLASH;
      }
      for (Enumeration<JarEntry> entries = jarFileInfo.jarFile.entries();
          entries.hasMoreElements(); ) {
        JarEntry entry = entries.nextElement();
        String entryPath = entry.getName();
        if (entryPath.startsWith(rootEntryPath) && PathMatcher.match(subPattern,
            entryPath.substring(rootEntryPath.length()))) {
          log.info("find jsp file [{}]", entryPath);
          File file = mkdirs(dir, entryPath);
          Files.copy(jarFileInfo.jarFile.getInputStream(entry), file.toPath());
        }
      }
    }
  }

  private File mkdirs(File dir, String name) throws IOException {
    int index = name.lastIndexOf(Strings.SLASH);
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
