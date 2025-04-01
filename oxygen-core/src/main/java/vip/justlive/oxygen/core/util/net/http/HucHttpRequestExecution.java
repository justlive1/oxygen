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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.Urls;

/**
 * HttpURLConnection 实现的执行器
 *
 * @author wubo
 * @since 3.0.4
 */
@Slf4j
public class HucHttpRequestExecution implements HttpRequestExecution {

  public static final HucHttpRequestExecution HUC = new HucHttpRequestExecution();

  @Override
  public HttpResponse execute(HttpRequest request) throws IOException {
    String httpUrl = Urls.urlWithQueryString(request);
    HttpURLConnection connection = buildConnection(httpUrl, request);
    if (request.getConnectTimeout() >= 0) {
      connection.setConnectTimeout(request.getConnectTimeout());
    }
    if (request.getReadTimeout() >= 0) {
      connection.setReadTimeout(request.getReadTimeout());
    }
    connection.setDoInput(true);
    connection.setInstanceFollowRedirects(request.isFollowRedirects());
    connection.setRequestMethod(request.getMethod().name());
    connection.setUseCaches(false);
    // add headers
    request.getHeaders().forEach(connection::addRequestProperty);
    boolean nonOutput = request.getMethod() == HttpMethod.GET || (request.getBody() == null
        && request.getHttpBody() != HttpBody.MULTIPART);
    connection.setDoOutput(!nonOutput);
    if (nonOutput) {
      connection.connect();
    } else {
      HttpBodyConverter converter = HttpBodyConverters.findCanWrite(request);
      if (converter == null) {
        log.error("No HttpBodyConverter for Request {}", request);
        throw Exceptions.fail("No HttpBodyConverter for HttpRequest");
      }
      byte[] bytes;
      try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
        converter.write(request, stream);
        bytes = stream.toByteArray();
      }
      connection.setFixedLengthStreamingMode(bytes.length);
      connection.connect();
      try (OutputStream out = connection.getOutputStream()) {
        out.write(bytes);
        out.flush();
      }
    }
    int code = connection.getResponseCode();
    String msg = connection.getResponseMessage();
    InputStream is = connection.getErrorStream();
    if (is == null && code < HttpURLConnection.HTTP_BAD_REQUEST) {
      is = connection.getInputStream();
    }
    return new HucHttpResponse(connection, code, msg, is, request.getCharset());
  }

  private HttpURLConnection buildConnection(String httpUrl, HttpRequest request)
      throws IOException {
    HttpURLConnection connection;
    if (request.getProxy() != null) {
      connection = (HttpURLConnection) URI.create(httpUrl).toURL()
          .openConnection(request.getProxy());
    } else {
      connection = (HttpURLConnection) URI.create(httpUrl).toURL().openConnection();
    }
    if (connection instanceof HttpsURLConnection https) {
      if (request.getSslSocketFactory() != null) {
        https.setSSLSocketFactory(request.getSslSocketFactory());
      }
      if (request.getHostnameVerifier() != null) {
        https.setHostnameVerifier(request.getHostnameVerifier());
      }
    }
    return connection;
  }


  /**
   * huc实现的response
   */
  @Getter
  @EqualsAndHashCode(callSuper = true)
  public static class HucHttpResponse extends HttpResponse {

    private final HttpURLConnection connection;

    public HucHttpResponse(HttpURLConnection connection, int code, String message, InputStream body,
        Charset charset) {
      super(code, message, body, charset);
      this.connection = connection;
    }

    @Override
    public Map<String, String> getHeaders() {
      if (super.getHeaders() == null) {
        Map<String, String> map = new HashMap<>(4);
        // Header field 0 is the 'HTTP/1.1 200' line for most HttpURLConnections, but not on GAE
        for (int i = 0; ; i++) {
          String name = this.connection.getHeaderFieldKey(i);
          if (name != null && !name.trim().isEmpty()) {
            map.put(name, this.connection.getHeaderField(i));
          } else if (i > 0) {
            break;
          }
        }
        setHeaders(map);
      }
      return super.getHeaders();
    }

    @Override
    public void close() throws IOException {
      super.close();
      this.connection.disconnect();
    }
  }
}
