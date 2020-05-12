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
package vip.justlive.oxygen.core.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import lombok.experimental.UtilityClass;

/**
 * 资源工具类
 *
 * @author wubo
 */
@UtilityClass
public class Urls {

  /**
   * URL协议-文件
   */
  public final String URL_PROTOCOL_FILE = "file";
  /**
   * URL协议-jar
   */
  public final String URL_PROTOCOL_JAR = "jar";
  /**
   * URL协议-war
   */
  public final String URL_PROTOCOL_WAR = "war";
  /**
   * URL协议-zip
   */
  public final String URL_PROTOCOL_ZIP = "zip";
  /**
   * URL协议-WebSphere jar
   */
  public final String URL_PROTOCOL_WSJAR = "wsjar";
  /**
   * URL协议-JBoss jar
   */
  public final String URL_PROTOCOL_VFSZIP = "vfszip";
  /**
   * war路径分隔符
   */
  public final String WAR_URL_SEPARATOR = "*/";
  /**
   * jar路径分隔符
   */
  public final String JAR_URL_SEPARATOR = "!/";

  /**
   * 判断URL是否是jar文件
   *
   * @param url 路径
   * @return true是jar文件
   */
  public boolean isJarURL(URL url) {
    String protocol = url.getProtocol();
    return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_WAR.equals(protocol)
        || URL_PROTOCOL_ZIP.equals(protocol) || URL_PROTOCOL_VFSZIP.equals(protocol)
        || URL_PROTOCOL_WSJAR.equals(protocol));
  }

  /**
   * String转换成URI
   *
   * @param url 路径
   * @return URI
   * @throws URISyntaxException 转换不了URI抛出异常
   */
  public URI toURI(String url) throws URISyntaxException {
    return new URI(url.replace(" ", "%20"));
  }

  /**
   * URL转换成URI
   *
   * @param url 路径
   * @return URI
   * @throws URISyntaxException 转换不了URI抛出异常
   */
  public URI toURI(URL url) throws URISyntaxException {
    return new URI(url.toString().replace(" ", "%20"));
  }

  /**
   * 获取相对资源路径
   *
   * @param rootPath 根路径
   * @param relative 相对路径
   * @return 路径
   */
  public String relativePath(String rootPath, String relative) {
    int last = rootPath.lastIndexOf(Strings.SLASH);
    String newPath = rootPath;
    if (last > -1) {
      newPath = rootPath.substring(0, last);
      if (!relative.startsWith(Strings.SLASH)) {
        newPath += Strings.SLASH;
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
  public String cutRootPath(String path) {
    String usePath = path;
    if (usePath.startsWith(Strings.SLASH)) {
      usePath = usePath.substring(Strings.SLASH.length());
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
  public String concat(String parent, String child) {
    MoreObjects.notNull(parent);
    MoreObjects.notNull(child);
    StringBuilder sb = new StringBuilder(parent);
    if (parent.endsWith(Strings.SLASH) && child.startsWith(Strings.SLASH)) {
      sb.deleteCharAt(sb.length() - 1);
    }
    if (!parent.endsWith(Strings.SLASH) && !child.startsWith(Strings.SLASH)) {
      sb.append(Strings.SLASH);
    }
    sb.append(child);
    if (sb.indexOf(Strings.SLASH) != 0) {
      sb.insert(0, Strings.SLASH);
    }
    if (sb.length() > 1 && sb.lastIndexOf(Strings.SLASH) == sb.length() - 1) {
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
  public JarFile getJarFile(String jarFileUrl) throws IOException {
    if (jarFileUrl.startsWith(Strings.FILE_PREFIX)) {
      try {
        return new JarFile(Urls.toURI(jarFileUrl).getSchemeSpecificPart());
      } catch (URISyntaxException ex) {
        // 失败可能是因为url不正确，去除协议尝试
        return new JarFile(jarFileUrl.substring(Strings.FILE_PREFIX.length()));
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
  public JarFileInfo getJarFileInfo(URL url) throws IOException {
    URLConnection con = url.openConnection();
    JarFile jarFile;
    String jarFileUrl;
    String rootEntryPath;
    if (con instanceof JarURLConnection) {
      JarURLConnection jarCon = (JarURLConnection) con;
      jarFile = jarCon.getJarFile();
      jarFileUrl = jarCon.getJarFileURL().toExternalForm();
      JarEntry jarEntry = jarCon.getJarEntry();
      rootEntryPath = (jarEntry != null ? jarEntry.getName() : Strings.EMPTY);
    } else {
      String urlFile = url.getFile();
      int separatorLength = WAR_URL_SEPARATOR.length();
      int separatorIndex = urlFile.indexOf(WAR_URL_SEPARATOR);
      if (separatorIndex == -1) {
        separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
        separatorLength = JAR_URL_SEPARATOR.length();
      }
      if (separatorIndex != -1) {
        jarFileUrl = urlFile.substring(0, separatorIndex);
        rootEntryPath = urlFile.substring(separatorIndex + separatorLength);
        jarFile = getJarFile(jarFileUrl);
      } else {
        jarFile = new JarFile(urlFile);
        jarFileUrl = urlFile;
        rootEntryPath = Strings.EMPTY;
      }
    }
    return new JarFileInfo(jarFile, jarFileUrl, rootEntryPath);
  }

  /**
   * url encode
   *
   * @param s string
   * @return encoded
   */
  public String urlEncode(String s) {
    return urlEncode(s, StandardCharsets.UTF_8);
  }

  /**
   * url encode
   *
   * @param s string
   * @param charset 字符集
   * @return encoded
   */
  public String urlEncode(String s, Charset charset) {
    try {
      return URLEncoder.encode(s, charset.name());
    } catch (UnsupportedEncodingException e) {
      // nothing
    }
    return s;
  }

  /**
   * url decode
   *
   * @param s string
   * @return decoded
   */
  public String urlDecode(String s) {
    return urlDecode(s, StandardCharsets.UTF_8);
  }

  /**
   * url decode
   *
   * @param s string
   * @param charset 字符集
   * @return decoded
   */
  public String urlDecode(String s, Charset charset) {
    try {
      return URLDecoder.decode(s, charset.name());
    } catch (UnsupportedEncodingException e) {
      // nothing
    }
    return s;
  }
}
