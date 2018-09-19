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
package vip.justlive.oxygen.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.util.Checks;
import vip.justlive.oxygen.core.util.ResourceUtils;

/**
 * classpath路径下的资源
 *
 * @author wubo
 */
public class ClassPathResource implements SourceResource {

  private String path;
  private ClassLoader classLoader;
  private Class<?> clazz;

  /**
   * 创建一个{@code ClassPathResource}<br> classloader为null可能资源访问不了
   *
   * @param path 路径
   */
  public ClassPathResource(String path) {
    this(path, (ClassLoader) null);
  }

  /**
   * 使用{@code ClassLoader}创建{@code ClassPathResource}
   *
   * @param path 路径
   * @param classLoader 类加载器
   */
  public ClassPathResource(String path, ClassLoader classLoader) {
    this.path = this.cutRootPath(Checks.notNull(path));
    this.classLoader = classLoader;
  }

  /**
   * 使用{@code Class}创建{@code ClassPathResource}
   *
   * @param path 路径
   * @param clazz 类
   */
  public ClassPathResource(String path, Class<?> clazz) {
    this.path = this.cutRootPath(Checks.notNull(path));
    this.clazz = clazz;
  }

  /**
   * 去除路径起始的/
   *
   * @param path 路径
   * @return 处理后的路径
   */
  String cutRootPath(String path) {
    String usePath = path;
    if (usePath.startsWith(Constants.ROOT_PATH)) {
      usePath = usePath.substring(Constants.ROOT_PATH.length());
    }
    return usePath;
  }

  @Override
  public String path() {
    return this.path;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    InputStream is;
    if (this.clazz != null) {
      is = this.clazz.getResourceAsStream(this.path);
    } else if (this.classLoader != null) {
      is = this.classLoader.getResourceAsStream(this.path);
    } else {
      is = ClassLoader.getSystemResourceAsStream(this.path);
    }
    if (is == null) {
      throw new FileNotFoundException(this.path + " cannot be opened because it does not exist");
    }
    return is;
  }

  @Override
  public boolean isFile() {
    URL url = getURL0();
    if (url != null) {
      return Constants.URL_PROTOCOL_FILE.equals(url.getProtocol());
    }
    return false;
  }

  @Override
  public File getFile() throws IOException {
    if (isFile()) {
      try {
        return new File(ResourceUtils.toURI(getURL()).getSchemeSpecificPart());
      } catch (URISyntaxException e) {
        return new File(getURL0().getFile());
      }
    }
    return null;
  }

  /**
   * 获取类加载器
   *
   * @return 类加载器
   */
  public ClassLoader getClassLoader() {
    if (this.clazz != null) {
      return this.clazz.getClassLoader();
    }
    return this.classLoader;
  }

  private URL getURL0() {
    if (this.clazz != null) {
      return this.clazz.getResource(this.path);
    } else if (this.classLoader != null) {
      return this.classLoader.getResource(this.path);
    } else {
      return ClassLoader.getSystemResource(this.path);
    }
  }

  /**
   * 获取资源URL
   *
   * @return URL
   */
  @Override
  public URL getURL() throws IOException {
    URL url = this.getURL0();
    if (url == null) {
      throw new FileNotFoundException(this.path + " cannot be found");
    }
    return url;
  }

  @Override
  public SourceResource createRelative(String path) {
    ClassPathResource resource = new ClassPathResource(ResourceUtils.relativePath(this.path, path));
    resource.classLoader = this.classLoader;
    resource.clazz = this.clazz;
    return resource;
  }

}
