/*
 * Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */

package vip.justlive.oxygen.core.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.util.MoreObjects;

/**
 * http request
 *
 * @author wubo
 */
public class HttpRequest {

  private final String url;
  private int connectTimeout;
  private int readTimeout;
  private boolean followRedirects;
  private HttpMethod method;
  private Charset charset = StandardCharsets.UTF_8;
  private Map<String, String> headers = new HashMap<>(4);
  private Object queryParam;
  private Object body;
  private Function<Object, byte[]> func;

  private HttpRequest(String url) {
    this.url = url;
  }

  /**
   * 构造request
   *
   * @param url http地址
   * @return request
   */
  public static HttpRequest url(String url) {
    return new HttpRequest(url);
  }

  /**
   * 构造get方式request
   *
   * @param url http地址
   * @return request
   */
  public static HttpRequest get(String url) {
    return url(url).method(HttpMethod.GET);
  }

  /**
   * 构造post方式request
   *
   * @param url http地址
   * @return request
   */
  public static HttpRequest post(String url) {
    return url(url).method(HttpMethod.POST);
  }

  /**
   * 设置http请求方式
   *
   * @param method http请求方式
   * @return request
   */
  public HttpRequest method(HttpMethod method) {
    this.method = method;
    return this;
  }

  /**
   * 设置连接超时时间
   *
   * @param connectTimeout 超时时间
   * @return request
   */
  public HttpRequest connectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  /**
   * 设置读取超时时间
   *
   * @param readTimeout 超时时间
   * @return request
   */
  public HttpRequest readTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

  /**
   * 设置是否自动跳转
   *
   * @param followRedirects 是否自动跳转
   * @return request
   */
  public HttpRequest followRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
    return this;
  }

  /**
   * 设置字符集
   *
   * @param charset 字符集
   * @return request
   */
  public HttpRequest charset(Charset charset) {
    this.charset = charset;
    return this;
  }

  /**
   * 添加header
   *
   * @param name name of header
   * @param value value of header
   * @return request
   */
  public HttpRequest addHeader(String name, String value) {
    headers.put(name, value);
    return this;
  }

  /**
   * 设置url的query参数
   *
   * @param queryParam query参数
   * @return request
   */
  public HttpRequest queryParam(Object queryParam) {
    this.queryParam = queryParam;
    return this;
  }

  /**
   * 增加表单请求体
   *
   * @param body 请求体
   * @return request
   */
  public HttpRequest formBody(Object body) {
    this.body = body;
    this.func = this::formBodyConvert;
    this.headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_FORM_URLENCODED);
    return this;
  }

  /**
   * 增加json请求体
   *
   * @param body 请求体
   * @return request
   */
  public HttpRequest jsonBody(String body) {
    this.body = body;
    this.func = this::jsonBodyConvert;
    this.headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
    return this;
  }

  /**
   * 增加自定义请求体
   *
   * @param body 请求体
   * @param func 请求体转换
   * @return request
   */
  public HttpRequest body(Object body, Function<Object, byte[]> func) {
    this.body = body;
    this.func = func;
    return this;
  }

  /**
   * 执行
   *
   * @return response
   * @throws IOException io异常
   */
  public HttpResponse execute() throws IOException {
    String httpUrl = this.url;
    if (queryParam != null) {
      String queryString = MoreObjects.beanToQueryString(queryParam, true);
      if (!url.contains(Constants.QUESTION_MARK)) {
        httpUrl += Constants.QUESTION_MARK;
      } else if (!url.endsWith(Constants.AND)) {
        httpUrl += Constants.AND;
      }
      httpUrl += queryString;
    }
    HttpURLConnection connection = (HttpURLConnection) new URL(httpUrl).openConnection();
    if (this.connectTimeout >= 0) {
      connection.setConnectTimeout(this.connectTimeout);
    }
    if (this.readTimeout >= 0) {
      connection.setReadTimeout(this.readTimeout);
    }
    connection.setInstanceFollowRedirects(this.followRedirects);
    connection.setRequestMethod(this.method.name());
    connection.setUseCaches(false);
    setContentType();
    // add headers
    headers.forEach(connection::addRequestProperty);
    if (body == null || this.method == HttpMethod.GET) {
      connection.setDoOutput(false);
      connection.connect();
    } else {
      connection.setDoOutput(true);
      byte[] bytes = this.func.apply(body);
      connection.setFixedLengthStreamingMode(bytes.length);
      connection.connect();
      try (OutputStream out = connection.getOutputStream()) {
        out.write(bytes);
        out.flush();
      }
    }
    return new HttpResponse(connection, charset);
  }

  private void setContentType() {
    String contentType = headers.get(Constants.CONTENT_TYPE);
    if (contentType != null && !contentType.contains(Constants.CHARSET)) {
      headers.put(Constants.CONTENT_TYPE, contentType + ";charset=" + charset.name());
    }
  }

  private byte[] formBodyConvert(Object obj) {
    if (obj == null) {
      return new byte[0];
    }
    return MoreObjects.beanToQueryString(obj).getBytes(charset);
  }

  private byte[] jsonBodyConvert(Object json) {
    if (json == null) {
      return new byte[0];
    }
    return json.toString().getBytes(charset);
  }
}
