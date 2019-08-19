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
package vip.justlive.oxygen.core.util;

import static vip.justlive.oxygen.core.constant.Constants.FILE_PREFIX;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * 资源工具类
 *
 * @author wubo
 */
@UtilityClass
public class ResourceUtils {

  /**
   * 判断URL是否是jar文件
   *
   * @param url 路径
   * @return true是jar文件
   */
  public static boolean isJarURL(URL url) {
    String protocol = url.getProtocol();
    return (Constants.URL_PROTOCOL_JAR.equals(protocol) || Constants.URL_PROTOCOL_WAR
        .equals(protocol) || Constants.URL_PROTOCOL_ZIP.equals(protocol)
        || Constants.URL_PROTOCOL_VFSZIP.equals(protocol) || Constants.URL_PROTOCOL_WSJAR
        .equals(protocol));
  }

  /**
   * String转换成URI
   *
   * @param url 路径
   * @return URI
   * @throws URISyntaxException 转换不了URI抛出异常
   */
  public static URI toURI(String url) throws URISyntaxException {
    return new URI(url.replace(" ", "%20"));
  }

  /**
   * URL转换成URI
   *
   * @param url 路径
   * @return URI
   * @throws URISyntaxException 转换不了URI抛出异常
   */
  public static URI toURI(URL url) throws URISyntaxException {
    return new URI(url.toString().replace(" ", "%20"));
  }

  /**
   * 获取相对资源路径
   *
   * @param rootPath 根路径
   * @param relative 相对路径
   * @return 路径
   */
  public static String relativePath(String rootPath, String relative) {
    int last = rootPath.lastIndexOf(Constants.PATH_SEPARATOR);
    String newPath = rootPath;
    if (last > -1) {
      newPath = rootPath.substring(0, last);
      if (!relative.startsWith(Constants.PATH_SEPARATOR)) {
        newPath += Constants.PATH_SEPARATOR;
      }
      newPath += relative;
    }
    return newPath;
  }

  /**
   * 去除路径起始的/
   *
   * @param path 路径
   * @return 处理后的路径
   */
  public static String cutRootPath(String path) {
    String usePath = path;
    if (usePath.startsWith(Constants.ROOT_PATH)) {
      usePath = usePath.substring(Constants.ROOT_PATH.length());
    }
    return usePath;
  }

  /**
   * 路径拼接
   *
   * @param parent 父路径
   * @param child 子路径
   * @return path
   */
  public static String concat(String parent, String child) {
    MoreObjects.notNull(parent);
    MoreObjects.notNull(child);
    StringBuilder sb = new StringBuilder(parent);
    if (parent.endsWith(Constants.ROOT_PATH) && child.startsWith(Constants.ROOT_PATH)) {
      sb.deleteCharAt(sb.length() - 1);
    }
    if (!parent.endsWith(Constants.ROOT_PATH) && !child.startsWith(Constants.ROOT_PATH)) {
      sb.append(Constants.ROOT_PATH);
    }
    sb.append(child);
    if (sb.indexOf(Constants.ROOT_PATH) != 0) {
      sb.insert(0, Constants.ROOT_PATH);
    }
    if (sb.length() > 1 && sb.lastIndexOf(Constants.ROOT_PATH) == sb.length() - 1) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  /**
   * 获取jar文件
   *
   * @param jarFileUrl jar文件路径
   * @return jar文件
   * @throws IOException io异常
   */
  public static JarFile getJarFile(String jarFileUrl) throws IOException {
    if (jarFileUrl.startsWith(FILE_PREFIX)) {
      try {
        return new JarFile(ResourceUtils.toURI(jarFileUrl).getSchemeSpecificPart());
      } catch (URISyntaxException ex) {
        // 失败可能是因为url不正确，去除协议尝试
        return new JarFile(jarFileUrl.substring(FILE_PREFIX.length()));
      }
    } else {
      return new JarFile(jarFileUrl);
    }
  }

  /**
   * 获取jar信息
   *
   * @param url url
   * @return jarFileInfo
   * @throws IOException io异常
   */
  public static JarFileInfo getJarFileInfo(URL url) throws IOException {
    URLConnection con = url.openConnection();
    JarFile jarFile;
    String jarFileUrl;
    String rootEntryPath;
    if (con instanceof JarURLConnection) {
      JarURLConnection jarCon = (JarURLConnection) con;
      jarFile = jarCon.getJarFile();
      jarFileUrl = jarCon.getJarFileURL().toExternalForm();
      JarEntry jarEntry = jarCon.getJarEntry();
      rootEntryPath = (jarEntry != null ? jarEntry.getName() : Constants.EMPTY);
    } else {
      String urlFile = url.getFile();
      int separatorLength = Constants.WAR_URL_SEPARATOR.length();
      int separatorIndex = urlFile.indexOf(Constants.WAR_URL_SEPARATOR);
      if (separatorIndex == -1) {
        separatorIndex = urlFile.indexOf(Constants.JAR_URL_SEPARATOR);
        separatorLength = Constants.JAR_URL_SEPARATOR.length();
      }
      if (separatorIndex != -1) {
        jarFileUrl = urlFile.substring(0, separatorIndex);
        rootEntryPath = urlFile.substring(separatorIndex + separatorLength);
        jarFile = getJarFile(jarFileUrl);
      } else {
        jarFile = new JarFile(urlFile);
        jarFileUrl = urlFile;
        rootEntryPath = Constants.EMPTY;
      }
    }
    return new JarFileInfo(jarFile, jarFileUrl, rootEntryPath);
  }

  public static class JarFileInfo {

    public final JarFile jarFile;
    public final String jarFileUrl;
    public final String rootEntryPath;

    public JarFileInfo(JarFile jarFile, String jarFileUrl, String rootEntryPath) {
      this.jarFile = jarFile;
      this.jarFileUrl = jarFileUrl;
      this.rootEntryPath = rootEntryPath;
    }
  }
}
