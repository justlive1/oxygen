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

package vip.justlive.oxygen.core.net.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import vip.justlive.oxygen.core.util.IoUtils;

/**
 * http response
 *
 * @author wubo
 */
@Data
public class HttpResponse implements Closeable {

  private final HttpURLConnection connection;
  private final int code;
  private final String message;
  private final InputStream body;
  private final Charset charset;
  private Map<String, String> headers;

  HttpResponse(HttpURLConnection connection, Charset charset) throws IOException {
    this.connection = connection;
    this.code = connection.getResponseCode();
    this.message = connection.getResponseMessage();
    InputStream in = connection.getErrorStream();
    if (in == null) {
      this.body = connection.getInputStream();
    } else {
      this.body = in;
    }
    this.charset = charset;
  }

  /**
   * 获取headers
   *
   * @return headers
   */
  public Map<String, String> getHeaders() {
    if (this.headers == null) {
      this.headers = new HashMap<>(4);
      // Header field 0 is the 'HTTP/1.1 200' line for most HttpURLConnections, but not on GAE
      for (int i = 0; ; i++) {
        String name = this.connection.getHeaderFieldKey(i);
        if (name != null && name.trim().length() > 0) {
          this.headers.put(name, this.connection.getHeaderField(i));
        } else if (i > 0) {
          break;
        }
      }
    }
    return this.headers;
  }

  /**
   * body转字符串
   *
   * @return body string
   * @throws IOException io异常
   */
  public String bodyAsString() throws IOException {
    return bodyAsString(charset);
  }

  /**
   * body转字符串
   *
   * @param charset 字符集
   * @return body string
   * @throws IOException io异常
   */
  public String bodyAsString(Charset charset) throws IOException {
    return IoUtils.toString(body, charset);
  }

  @Override
  public void close() throws IOException {
    if (this.body != null) {
      IoUtils.drain(this.body);
      this.body.close();
    }
    this.connection.disconnect();
  }
}