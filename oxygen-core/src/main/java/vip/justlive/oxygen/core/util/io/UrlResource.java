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
package vip.justlive.oxygen.core.util.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.base.Urls;

/**
 * {@code URL} 类型的资源
 *
 * @author wubo
 */
public class UrlResource implements SourceResource {

  private final URL url;

  /**
   * 使用{@code URL}创建{@code UrlResource}
   *
   * @param url URL
   */
  public UrlResource(URL url) {
    this.url = MoreObjects.notNull(url);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    URLConnection con = this.url.openConnection();
    try {
      return con.getInputStream();
    } catch (IOException ex) {
      // 关闭HTTP连接
      if (con instanceof HttpURLConnection) {
        ((HttpURLConnection) con).disconnect();
      }
      throw ex;
    }
  }

  @Override
  public String path() {
    return url.toString();
  }

  @Override
  public boolean isFile() {
    return Urls.URL_PROTOCOL_FILE.equals(url.getProtocol());
  }

  @Override
  public File getFile() {
    if (isFile()) {
      try {
        return new File(Urls.toURI(this.url).getSchemeSpecificPart());
      } catch (URISyntaxException e) {
        return new File(url.getFile());
      }
    }
    return null;
  }

  @Override
  public URL getUrl() {
    return this.url;
  }

  @Override
  public SourceResource createRelative(String path) throws MalformedURLException {
    if (path.startsWith(Strings.SLASH)) {
      path = path.substring(Strings.SLASH.length());
    }
    return new UrlResource(new URL(this.url, path));
  }
}
