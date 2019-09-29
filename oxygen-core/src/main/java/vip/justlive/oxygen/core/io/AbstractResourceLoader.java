/*
 * Copyright (C) 2019 the original author or authors.
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
package vip.justlive.oxygen.core.io;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.PathMatcher;
import vip.justlive.oxygen.core.util.Strings;
import vip.justlive.oxygen.core.util.Urls;
import vip.justlive.oxygen.core.util.Urls.JarFileInfo;

/**
 * 抽象资源加载器
 * <br>
 * 支持查找classpath下配置文件，例如 classpath:/config/dev.properties,classpath*:/config/*.properties
 * <br>
 * 支持查找文件系统下配置文件，例如 file:/home/dev.properties, file:D:/conf/dev.properties
 *
 * @author wubo
 */
@Slf4j
public abstract class AbstractResourceLoader {

  /**
   * 类加载器
   */
  protected ClassLoader loader;

  /**
   * 路径匹配器
   */
  protected PathMatcher matcher = new PathMatcher();

  /**
   * 资源列表
   */
  protected List<SourceResource> resources = new ArrayList<>();

  /**
   * 未找到是否忽略，启用则跳过，否则抛出异常
   */
  @Getter
  @Setter
  protected boolean ignoreNotFound;

  /**
   * 文件编码
   */
  @Getter
  @Setter
  protected String encoding;

  /**
   * 字符集
   */
  @Getter
  @Setter
  protected Charset charset;

  /**
   * 是否已初始化过
   */
  protected volatile boolean ready;

  /**
   * 初始化
   */
  public abstract void init();

  /**
   * 获取资源列表
   *
   * @return 资源列表
   */
  public List<SourceResource> resources() {
    return this.resources;
  }

  /**
   * 获取资源Reader
   *
   * @param resource 源资源
   * @return Reader
   * @throws IOException io异常
   */
  protected Reader getReader(SourceResource resource) throws IOException {
    if (this.charset != null) {
      return new InputStreamReader(resource.getInputStream(), this.charset);
    } else if (this.encoding != null) {
      return new InputStreamReader(resource.getInputStream(), this.encoding);
    } else {
      return new InputStreamReader(resource.getInputStream());
    }
  }

  /**
   * 解析路径
   *
   * @param locations 路径
   * @return 资源列表
   */
  protected List<SourceResource> parse(String... locations) {
    List<SourceResource> list = new LinkedList<>();
    for (String location : locations) {
      try {
        log.info("parsing resource for [{}]", location);
        if (location.startsWith(Strings.ALL_CLASSPATH_PREFIX)) {
          list.addAll(this.resolveAllClassPathResource(
              location.substring(Strings.ALL_CLASSPATH_PREFIX.length())));
        } else if (location.startsWith(Strings.CLASSPATH_PREFIX)) {
          list.addAll(
              this.resolveClassPathResource(location.substring(Strings.CLASSPATH_PREFIX.length())));
        } else if (location.startsWith(Strings.FILE_PREFIX)) {
          list.addAll(this.resolveFileSystemResource(location));
        } else {
          list.addAll(this.resolveClassPathResource(location));
        }
      } catch (IOException e) {
        log.warn("location [{}] cannot find resource", location);
        if (!ignoreNotFound) {
          throw Exceptions.wrap(e);
        }
      }
    }
    return list;
  }

  /**
   * 处理所有classpath下的资源
   *
   * @param location 路径
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> resolveAllClassPathResource(String location) throws IOException {
    List<SourceResource> list = new LinkedList<>();
    if (matcher.isPattern(location)) {
      list.addAll(this.findMatchPath(location, true));
    } else {
      Enumeration<URL> res = loader.getResources(Urls.cutRootPath(location));
      if (res == null) {
        return list;
      }
      while (res.hasMoreElements()) {
        URL url = res.nextElement();
        list.add(new UrlResource(url));
      }
    }
    return list;
  }

  /**
   * 处理classpath下的资源
   *
   * @param location 路径
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> resolveClassPathResource(String location) throws IOException {
    List<SourceResource> list = new LinkedList<>();
    if (matcher.isPattern(location)) {
      list.addAll(this.findMatchPath(location, false));
    } else {
      list.add(new ClassPathResource(location, loader));
    }
    return list;
  }

  /**
   * 处理文件系统下的资源
   *
   * @param location 路径
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> resolveFileSystemResource(String location) throws IOException {
    List<SourceResource> list = new LinkedList<>();
    if (matcher.isPattern(location)) {
      list.addAll(this.findMatchPath(location, false));
    } else {
      list.add(new FileSystemResource(location.substring(Strings.FILE_PREFIX.length())));
    }
    return list;
  }

  /**
   * 获取匹配的路径
   *
   * @param location 路径
   * @param multi is classpath*
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> findMatchPath(String location, boolean multi) throws IOException {
    List<SourceResource> all = new LinkedList<>();
    String rootPath = matcher.getRootDir(location);
    String subPattern = location.substring(rootPath.length());
    List<SourceResource> rootResources;
    if (multi) {
      Enumeration<URL> urls = this.loader.getResources(Urls.cutRootPath(rootPath));
      rootResources = new LinkedList<>();
      while (urls.hasMoreElements()) {
        rootResources.add(new UrlResource(urls.nextElement()));
      }
    } else {
      rootResources = this.parse(rootPath);
    }

    for (SourceResource resource : rootResources) {
      URL rootUrl = resource.getURL();
      if (Urls.URL_PROTOCOL_FILE.equals(rootUrl.getProtocol())) {
        all.addAll(this.findFileMatchPath(resource, subPattern));
      } else if (Urls.isJarURL(rootUrl)) {
        all.addAll(this.findJarMatchPath(resource, rootUrl, subPattern));
      }
    }
    return all;
  }

  /**
   * 获取资源下匹配的jar中的文件
   *
   * @param resource 源
   * @param rootUrl 根路径
   * @param subPattern 匹配串
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> findJarMatchPath(SourceResource resource, URL rootUrl,
      String subPattern) throws IOException {
    List<SourceResource> all = new LinkedList<>();
    JarFileInfo jarFileInfo = Urls.getJarFileInfo(rootUrl);
    try {
      this.look(resource, subPattern, all, jarFileInfo.jarFile, jarFileInfo.jarFileUrl,
          jarFileInfo.rootEntryPath);
    } finally {
      jarFileInfo.jarFile.close();
    }
    return all;
  }

  /**
   * 查找jar中匹配的资源
   *
   * @param resource 源
   * @param subPattern 匹配串
   * @param all 所有资源
   * @param jarFile jar文件
   * @param jarFileUrl jar文件路径
   * @param rootEntryPath 根路径
   * @throws IOException io异常
   */
  private void look(SourceResource resource, String subPattern, List<SourceResource> all,
      JarFile jarFile, String jarFileUrl, String rootEntryPath) throws IOException {
    if (log.isDebugEnabled()) {
      log.debug("Looking for matching resources in jar file [{}]", jarFileUrl);
    }
    if (rootEntryPath.length() > 0 && !rootEntryPath.endsWith(Strings.SLASH)) {
      rootEntryPath += Strings.SLASH;
    }
    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
      JarEntry entry = entries.nextElement();
      String entryPath = entry.getName();
      if (entryPath.startsWith(rootEntryPath)) {
        String relativePath = entryPath.substring(rootEntryPath.length());
        if (matcher.match(subPattern, relativePath)) {
          all.add(resource.createRelative(relativePath));
        }
      }
    }
  }

  /**
   * 获取资源下匹配的文件
   *
   * @param resource 源
   * @param subPattern 匹配串
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> findFileMatchPath(SourceResource resource, String subPattern)
      throws IOException {
    List<SourceResource> list = new LinkedList<>();
    matcher.findMatchedFiles(resource.getFile().getAbsoluteFile(), subPattern)
        .forEach(file -> list.add(new FileSystemResource(file)));
    return list;
  }

}
