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
package vip.justlive.oxygen.web.tomcat;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.servlet.ServletContext;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.Jar;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.buf.UriUtil;
import org.apache.tomcat.util.compat.JreCompat;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.scan.JarFactory;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * fat-jar scanner
 * <br>
 * 使用ide时，WebappClassLoader.getParent() 是 AppClassLoader，只需要扫描 WebappClassLoader 和 AppClassLoader
 * <br>
 * 使用springboot插件打包的fat-jar时，WebappClassLoader.getParent() 是 LaunchedURLClassLoader，扫描WebappClassLoader
 * 和 LaunchedURLClassLoader
 *
 * @author wubo
 */
public class FatJarScanner implements JarScanner {

  /**
   * The string resources for this package.
   */
  private static final StringManager SM = StringManager.getManager("org.apache.tomcat.util.scan");
  private static final Set<ClassLoader> CLASSLOADER_HIERARCHY;

  static {
    Set<ClassLoader> cls = new HashSet<>();

    ClassLoader cl = FatJarScanner.class.getClassLoader();
    while (cl != null) {
      cls.add(cl);
      cl = cl.getParent();
    }
    CLASSLOADER_HIERARCHY = Collections.unmodifiableSet(cls);
  }

  private final Log log = LogFactory.getLog(FatJarScanner.class);
  /**
   * Controls the filtering of the results from the scan for JARs
   */
  private JarScanFilter jarScanFilter = new StandardJarScanFilter();

  /**
   * Since class loader hierarchies can get complicated, this method attempts to apply the following
   * rule: A class loader is a web application class loader unless it loaded this class
   * (StandardJarScanner) or is a parent of the class loader that loaded this class.
   *
   * This should mean: the webapp class loader is an application class loader the shared class
   * loader is an application class loader the server class loader is not an application class
   * loader the common class loader is not an application class loader the system class loader is
   * not an application class loader the bootstrap class loader is not an application class loader
   */
  private static boolean isWebappClassLoader(ClassLoader classLoader) {
    return !CLASSLOADER_HIERARCHY.contains(classLoader);
  }

  @Override
  public JarScanFilter getJarScanFilter() {
    return jarScanFilter;
  }

  @Override
  public void setJarScanFilter(JarScanFilter jarScanFilter) {
    this.jarScanFilter = jarScanFilter;
  }

  /**
   * Scan the provided ServletContext and class loade r for JAR files. Each JAR file found will be
   * passed to the callback handler to be processed.
   *
   * @param scanType The type of JAR scan to perform. This is passed to the filter which uses it to
   * determine how to filter the results
   * @param context The ServletContext - used to locate and access WEB-INF/lib
   * @param callback The handler to process any JARs found
   */
  @Override
  public void scan(JarScanType scanType, ServletContext context, JarScannerCallback callback) {

    if (log.isTraceEnabled()) {
      log.trace(SM.getString("jarScan.webinflibStart"));
    }

    Set<URL> processedURLs = new HashSet<>();
    // Scan WEB-INF/lib
    doScanWebInfLib(scanType, context, callback, processedURLs);
    // Scan WEB-INF/classes
    doScanWebInf(context, callback, processedURLs);
    // Scan the classpath
    doScanClassPath(scanType, context, callback, processedURLs);
  }

  private void doScanWebInfLib(JarScanType scanType, ServletContext context,
      JarScannerCallback callback, Set<URL> processedURLs) {
    Set<String> dirList = context.getResourcePaths(Constants.WEB_INF_LIB);
    if (dirList == null) {
      return;
    }
    for (String path : dirList) {
      if (path.endsWith(Constants.JAR_EXT) && getJarScanFilter()
          .check(scanType, path.substring(path.lastIndexOf(Constants.ROOT_PATH) + 1))) {
        // Need to scan this JAR
        if (log.isDebugEnabled()) {
          log.debug(SM.getString("jarScan.webinflibJarScan", path));
        }
        URL url = null;
        try {
          url = context.getResource(path);
          processedURLs.add(url);
          process(scanType, callback, url, path, true, null);
        } catch (IOException e) {
          log.warn(SM.getString("jarScan.webinflibFail", url), e);
        }
      } else {
        if (log.isTraceEnabled()) {
          log.trace(SM.getString("jarScan.webinflibJarNoScan", path));
        }
      }
    }
  }

  private void doScanWebInf(ServletContext context, JarScannerCallback callback,
      Set<URL> processedURLs) {
    try {
      URL webInfURL = context.getResource(Constants.WEB_INF_CLASSES);
      if (webInfURL != null) {
        // WEB-INF/classes will also be included in the URLs returned
        // by the web application class loader so ensure the class path
        // scanning below does not re-scan this location.
        processedURLs.add(webInfURL);

        URL url = context.getResource(Constants.WEB_INF_CLASSES + Constants.META_INF_PATH);
        if (url != null) {
          callback.scanWebInfClasses();
        }
      }
    } catch (IOException e) {
      log.warn(SM.getString("jarScan.webinfclassesFail"), e);
    }
  }

  private void doScanClassPath(JarScanType scanType, ServletContext context,
      JarScannerCallback callback, Set<URL> processedURLs) {
    if (log.isTraceEnabled()) {
      log.trace(SM.getString("jarScan.classloaderStart"));
    }
    ClassLoader classLoader = context.getClassLoader();

    ClassLoader stopLoader = null;
    if (classLoader.getParent() != null) {
      // Stop when we reach the bootstrap class loader
      stopLoader = classLoader.getParent().getParent();
    }

    // JARs are treated as application provided until the common class
    // loader is reached.
    boolean isWebapp = true;

    // Use a Deque so URLs can be removed as they are processed
    // and new URLs can be added as they are discovered during
    // processing.
    Deque<URL> classPathUrlsToProcess = new LinkedList<>();

    while (classLoader != null && classLoader != stopLoader) {
      if (classLoader instanceof URLClassLoader) {
        if (isWebapp) {
          isWebapp = isWebappClassLoader(classLoader);
        }

        classPathUrlsToProcess.addAll(Arrays.asList(((URLClassLoader) classLoader).getURLs()));

        processURLs(scanType, callback, processedURLs, isWebapp, classPathUrlsToProcess);
      }
      classLoader = classLoader.getParent();
    }

    if (JreCompat.isJre9Available()) {
      // The application and platform class loaders are not
      // instances of URLClassLoader. Use the class path in this
      // case.
      addClassPath(classPathUrlsToProcess);
      // Also add any modules
      JreCompat.getInstance().addBootModulePath(classPathUrlsToProcess);
      processURLs(scanType, callback, processedURLs, false, classPathUrlsToProcess);
    }
  }

  private void processURLs(JarScanType scanType, JarScannerCallback callback,
      Set<URL> processedURLs, boolean isWebapp, Deque<URL> classPathUrlsToProcess) {
    while (!classPathUrlsToProcess.isEmpty()) {
      URL url = classPathUrlsToProcess.pop();

      if (processedURLs.contains(url)) {
        // Skip this URL it has already been processed
        continue;
      }
      if (log.isDebugEnabled()) {
        log.debug(SM.getString("jarScan.classloaderJarScan", url));
      }
      try {
        processedURLs.add(url);
        process(scanType, callback, url, null, isWebapp, classPathUrlsToProcess);
      } catch (IOException ioe) {
        log.warn(SM.getString("jarScan.classloaderFail", url), ioe);
      }
    }
  }

  private void addClassPath(Deque<URL> classPathUrlsToProcess) {
    String classPath = System.getProperty("java.class.path");

    if (classPath == null || classPath.length() == 0) {
      return;
    }

    String[] classPathEntries = classPath.split(File.pathSeparator);
    for (String classPathEntry : classPathEntries) {
      File f = new File(classPathEntry);
      try {
        classPathUrlsToProcess.add(f.toURI().toURL());
      } catch (MalformedURLException e) {
        log.warn(SM.getString("jarScan.classPath.badEntry", classPathEntry), e);
      }
    }
  }

  /**
   * Scan a URL for JARs with the optional extensions to look at all files and all directories.
   */
  private void process(JarScanType scanType, JarScannerCallback callback, URL url,
      String webappPath, boolean isWebapp, Deque<URL> classPathUrlsToProcess) throws IOException {

    if (log.isTraceEnabled()) {
      log.trace(SM.getString("jarScan.jarUrlStart", url));
    }

    if (Constants.URL_PROTOCOL_JAR.equals(url.getProtocol()) || url.getPath()
        .endsWith(Constants.JAR_EXT)) {
      try (Jar jar = JarFactory.newInstance(url)) {
        processManifest(jar, isWebapp, classPathUrlsToProcess);
        callback.scan(jar, webappPath, isWebapp);
      }
    } else if (Constants.URL_PROTOCOL_FILE.equals(url.getProtocol())) {
      processFile(scanType, callback, url, webappPath, isWebapp, classPathUrlsToProcess);
    }
  }

  private void processFile(JarScanType scanType, JarScannerCallback callback, URL url,
      String webappPath, boolean isWebapp, Deque<URL> classPathUrlsToProcess) throws IOException {
    try {
      File f = new File(url.toURI());
      if (f.isFile()) {
        // Treat this file as a JAR
        try (Jar jar = JarFactory.newInstance(UriUtil.buildJarUrl(f))) {
          processManifest(jar, isWebapp, classPathUrlsToProcess);
          callback.scan(jar, webappPath, isWebapp);
        }
      } else if (f.isDirectory()) {
        if (scanType == JarScanType.PLUGGABILITY) {
          callback.scan(f, webappPath, isWebapp);
        } else {
          if (new File(f.getAbsoluteFile(), Constants.META_INF).isDirectory()) {
            callback.scan(f, webappPath, isWebapp);
          }
        }
      }
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }


  private void processManifest(Jar jar, boolean isWebapp, Deque<URL> classPathUrlsToProcess)
      throws IOException {

    // Not processed for web application JARs nor if the caller did not provide a Deque of URLs to append to.
    if (isWebapp || classPathUrlsToProcess == null) {
      return;
    }

    Manifest manifest = jar.getManifest();
    if (manifest == null) {
      return;
    }
    Attributes attributes = manifest.getMainAttributes();
    String classPathAttribute = attributes.getValue("Class-Path");
    if (classPathAttribute == null) {
      return;
    }
    String[] classPathEntries = classPathAttribute.split(" ");
    for (String classPathEntry : classPathEntries) {
      classPathEntry = classPathEntry.trim();
      if (classPathEntry.length() == 0) {
        continue;
      }
      URL jarURL = jar.getJarFileURL();
      try {
        /*
         * Note: Resolving the relative URLs from the manifest has the
         *       potential to introduce security concerns. However, since
         *       only JARs provided by the container and NOT those provided
         *       by web applications are processed, there should be no
         *       issues.
         *       If this feature is ever extended to include JARs provided
         *       by web applications, checks should be added to ensure that
         *       any relative URL does not step outside the web application.
         */
        URI classPathEntryURI = jar.getJarFileURL().toURI().resolve(classPathEntry);
        classPathUrlsToProcess.add(classPathEntryURI.toURL());
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug(SM.getString("jarScan.invalidUri", jarURL), e);
        }
      }
    }
  }

}
