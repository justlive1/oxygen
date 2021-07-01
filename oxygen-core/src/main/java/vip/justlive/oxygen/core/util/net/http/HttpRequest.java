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

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import lombok.Getter;

/**
 * http request
 *
 * @author wubo
 */
@Getter
public class HttpRequest {

  private final String url;
  private final Map<String, String> headers = new HashMap<>(4);
  private int connectTimeout;
  private int readTimeout;
  private boolean followRedirects = true;
  private HttpMethod method;
  private HttpBody httpBody = HttpBody.NONE;
  private Charset charset = StandardCharsets.UTF_8;
  private Object queryParam;
  private Object body;
  private List<Part> parts;
  private Function<Object, byte[]> func;
  private Proxy proxy;
  private SSLSocketFactory sslSocketFactory;
  private HostnameVerifier hostnameVerifier;
  private HttpRequestExecution httpRequestExecution;
  private List<HttpRequestInterceptor> interceptors;

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
   * 设置代理
   *
   * @param proxy 代理
   * @return request
   */
  public HttpRequest proxy(Proxy proxy) {
    this.proxy = proxy;
    return this;
  }

  /**
   * 设置ssl工厂
   *
   * @param sslSocketFactory factory
   * @return request
   */
  public HttpRequest sslSocketFactory(SSLSocketFactory sslSocketFactory) {
    this.sslSocketFactory = sslSocketFactory;
    return this;
  }

  /**
   * 设置主机名校验
   *
   * @param hostnameVerifier verifier
   * @return request
   */
  public HttpRequest hostnameVerifier(HostnameVerifier hostnameVerifier) {
    this.hostnameVerifier = hostnameVerifier;
    return this;
  }

  /**
   * 设置执行器
   *
   * @param httpRequestExecution 执行器
   * @return request
   */
  public HttpRequest httpRequestExecution(HttpRequestExecution httpRequestExecution) {
    this.httpRequestExecution = httpRequestExecution;
    return this;
  }

  /**
   * 设置拦截器
   *
   * @param interceptors 拦截器
   * @return request
   */
  public HttpRequest interceptors(List<HttpRequestInterceptor> interceptors) {
    this.interceptors = interceptors;
    return this;
  }

  /**
   * 添加header
   *
   * @param name  name of header
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
    this.httpBody = HttpBody.FORM;
    return this;
  }

  /**
   * 增加json请求体
   *
   * @param body 请求体
   * @return request
   */
  public HttpRequest jsonBody(Object body) {
    this.body = body;
    this.httpBody = HttpBody.JSON;
    return this;
  }

  /**
   * multipart body
   *
   * @param name 参数名称
   * @param file 文件
   * @return request
   * @since 3.0.4
   */
  public HttpRequest multipart(String name, File file) {
    return multipart(name, file, null);
  }

  /**
   * multipart body
   *
   * @param name  参数名称
   * @param value 值
   * @return request
   * @since 3.0.4
   */
  public HttpRequest multipart(String name, String value) {
    this.httpBody = HttpBody.MULTIPART;
    if (parts == null) {
      parts = new ArrayList<>();
    }
    parts.add(new Part(name, value));
    return this;
  }

  /**
   * multipart body
   *
   * @param body 请求体
   * @return request
   */
  public HttpRequest multipart(Object body) {
    this.body = body;
    this.httpBody = HttpBody.MULTIPART;
    return this;
  }

  /**
   * multipart body
   *
   * @param name     参数名称
   * @param file     文件
   * @param filename 文件名称
   * @return request
   * @since 3.0.4
   */
  public HttpRequest multipart(String name, File file, String filename) {
    this.httpBody = HttpBody.MULTIPART;
    if (parts == null) {
      parts = new ArrayList<>();
    }
    parts.add(new Part(name, file, filename));
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
    this.httpBody = HttpBody.OTHERS;
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
    Iterator<HttpRequestInterceptor> iterable = null;
    if (interceptors != null) {
      iterable = interceptors.iterator();
    }
    if (httpRequestExecution == null) {
      httpRequestExecution = HucHttpRequestExecution.HUC;
    }
    return new IteratorHttpRequestInterceptor(httpRequestExecution, iterable).execute(this);
  }

  @Override
  public String toString() {
    String res = "{url:" + url + ", method:" + method + ", headers:" + headers;
    if (queryParam != null) {
      res += ", queryParam:" + queryParam;
    }
    if (body != null) {
      res += ", body:" + body;
    }
    if (parts != null) {
      res += ", parts:" + parts;
    }
    res += "}";
    return res;
  }
}
