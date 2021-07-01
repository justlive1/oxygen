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

package vip.justlive.oxygen.core.util.net.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import lombok.Data;
import vip.justlive.oxygen.core.util.io.IoUtils;

/**
 * http response
 *
 * @author wubo
 */
@Data
public class HttpResponse implements Closeable {

  private final int code;
  private final String message;
  private final InputStream body;
  private final Charset charset;
  private Map<String, String> headers;
  private String bodyString;

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
  public synchronized String bodyAsString(Charset charset) throws IOException {
    if (bodyString == null) {
      bodyString = IoUtils.toString(body, charset);
    }
    return bodyString;
  }

  @Override
  public void close() throws IOException {
    if (this.body != null) {
      IoUtils.drain(this.body);
      this.body.close();
    }
  }
}
