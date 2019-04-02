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
package vip.justlive.oxygen.core.scan;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.PathMatcher;
import vip.justlive.oxygen.core.util.ResourceUtils;
import vip.justlive.oxygen.core.util.ResourceUtils.JarFileInfo;

/**
 * 默认class scanner实现，采用类加载方式
 *
 * @author wubo
 */
@Slf4j
public class DefaultClassScanner implements ClassScanner {

  private static final String CLASS_SUFFIX = ".class";
  private static final String CLASS_PATH_MULTI_SUFFIX = "/**/*" + CLASS_SUFFIX;

  /**
   * 路径匹配器
   */
  private PathMatcher matcher = new PathMatcher();
  /**
   * 扫描包路径
   */
  private ClassLoader loader;

  public DefaultClassScanner() {
    this(ClassUtils.getDefaultClassLoader());
  }

  public DefaultClassScanner(ClassLoader loader) {
    this.loader = loader;
  }

  @Override
  public Set<Class<?>> scan(String... packages) {
    Set<Class<?>> classes = new HashSet<>();
    for (String pkg : packages) {
      String transfer = pkg.replace(Constants.DOT, Constants.ROOT_PATH) + CLASS_PATH_MULTI_SUFFIX;
      try {
        this.findMatchPath(transfer, classes);
      } catch (IOException e) {
        log.warn("scan package [{}] -> [{}] fail", pkg, transfer, e);
      }
    }
    return classes;
  }

  private void findMatchPath(String location, Set<Class<?>> classes) throws IOException {
    String rootPath = matcher.getRootDir(location);
    String subPattern = location.substring(rootPath.length());
    Enumeration<URL> urls = this.loader.getResources(ResourceUtils.cutRootPath(rootPath));
    while (urls.hasMoreElements()) {
      URL rootUrl = urls.nextElement();
      if (Constants.URL_PROTOCOL_FILE.equals(rootUrl.getProtocol())) {
        this.findFileMatchPath(rootPath, rootUrl, subPattern, classes);
      } else if (ResourceUtils.isJarURL(rootUrl)) {
        this.findJarMatchPath(rootUrl, subPattern, classes);
      }
    }
  }

  private void findFileMatchPath(String rootPath, URL rootUrl, String subPattern,
      Set<Class<?>> classes) {
    File rootDir;
    try {
      rootDir = new File(ResourceUtils.toURI(rootUrl).getSchemeSpecificPart());
    } catch (URISyntaxException e) {
      rootDir = new File(rootUrl.getFile());
    }
    Set<File> matchedFiles = matcher.findMatchedFiles(rootDir, subPattern);
    for (File file : matchedFiles) {
      String className = pathToClassName(rootPath, rootDir, file)
          .replace(CLASS_SUFFIX, Constants.EMPTY);
      try {
        classes.add(loader.loadClass(className));
      } catch (Throwable e) {
        log.warn("class [{}] cannot load ", className, e);
      }
    }
  }

  private void findJarMatchPath(URL rootUrl, String subPattern, Set<Class<?>> classes)
      throws IOException {
    JarFileInfo jarFileInfo = ResourceUtils.getJarFileInfo(rootUrl);
    try {
      if (log.isDebugEnabled()) {
        log.debug("Looking for matching resources in jar file [" + jarFileInfo.jarFileUrl + "]");
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
          String className = entryPath.replace(Constants.ROOT_PATH, Constants.DOT)
              .replace(CLASS_SUFFIX, Constants.EMPTY);
          try {
            classes.add(loader.loadClass(className));
          } catch (Throwable e) {
            log.warn("class [{}] cannot load ", className, e);
          }
        }
      }
    } finally {
      jarFileInfo.jarFile.close();
    }
  }

  private String pathToClassName(String rootPath, File rootDir, File subFile) {
    return pathToClassName(rootPath,
        subFile.getAbsolutePath().replace(rootDir.getAbsolutePath(), Constants.EMPTY)
            .replace(File.separator, Constants.DOT));
  }

  private String pathToClassName(String rootPath, String subPath) {
    String className = rootPath.replace(Constants.ROOT_PATH, Constants.DOT);
    if (!className.endsWith(Constants.DOT)) {
      className = className.concat(Constants.DOT);
    }
    if (subPath.startsWith(Constants.DOT)) {
      return className.concat(subPath.substring(1));
    }
    return className.concat(subPath);
  }

}
