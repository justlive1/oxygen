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
package vip.justlive.oxygen.web.http;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.net.http.HttpMethod;
import vip.justlive.oxygen.web.router.RouteHandler;
import vip.justlive.oxygen.web.router.StaticRouteHandler;

/**
 * Request
 *
 * @author wubo
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Request implements Serializable {

  public static final String ORIGINAL_REQUEST = "_ORIGINAL_REQUEST";
  public static final String PATH_VARS = "_PATH_VARS:";

  private static final long serialVersionUID = 1L;

  private static final ThreadLocal<Request> LOCAL = new ThreadLocal<>();
  /**
   * 请求类型
   */
  final HttpMethod method;
  /**
   * 请求地址
   */
  final String requestUri;
  /**
   * 协议 http/1.1
   */
  final String protocol;
  /**
   * query params
   */
  private final Map<String, String[]> params = new HashMap<>(4);
  /**
   * headers
   */
  private final Map<String, String[]> headers = new HashMap<>(4);
  /**
   * cookies
   */
  private final Map<String, Cookie> cookies = new HashMap<>(4);
  /**
   * body params
   */
  private final Map<String, Object> bodyParams = new HashMap<>(4);
  /**
   * attributes
   */
  private final transient Map<String, Object> attributes = new HashMap<>(2);
  /**
   * body
   */
  byte[] body;
  /**
   * ip:port
   */
  String host;
  /**
   * 请求路径 去除queryString
   */
  String path;
  /**
   * 容器路径
   */
  String contextPath;
  /**
   * 客户端ip
   */
  String remoteAddress;
  /**
   * 主机名
   */
  String domain;
  /**
   * 端口
   */
  Integer port;
  /**
   * 内容类型
   */
  String contentType;
  /**
   * 字符集编码
   */
  String encoding = StandardCharsets.UTF_8.name();
  /**
   * queryString
   */
  String queryString;
  /**
   * multipart
   */
  Multipart multipart;
  /**
   * session
   */
  Session session;
  /**
   * 是否https
   */
  boolean secure;

  transient RouteHandler routeHandler;
  /**
   * 异常
   */
  transient Exception exception;

  public Request(HttpMethod method, String requestUri, String protocol, String contextPath,
      byte[] body) {
    this.method = method;
    this.requestUri = requestUri;
    this.protocol = protocol;
    this.contextPath = contextPath;
    this.body = body;
  }

  /**
   * 当前请求的request
   *
   * @return request
   */
  public static Request current() {
    return LOCAL.get();
  }

  /**
   * 清除request数据
   */
  public static void clear() {
    LOCAL.remove();
  }

  /**
   * 设置到当前线程存储
   */
  public void local() {
    LOCAL.set(this);
  }

  /**
   * 是否为multipart请求
   *
   * @return true表示是multipart请求
   */
  public boolean isMultipart() {
    return multipart != null;
  }

  /**
   * 根据key获取查询参数 最多返回第一个值
   *
   * @param key 键
   * @return value
   */
  public String getParam(String key) {
    String[] values = getParams(key);
    if (values.length > 0) {
      return values[0];
    }
    return null;
  }

  /**
   * 根据key获取查询参数数组
   *
   * @param key 键
   * @return values
   */
  public String[] getParams(String key) {
    String[] values = params.get(key);
    if (values != null) {
      return values;
    }
    return Strings.EMPTY_ARRAY;
  }

  /**
   * 根据key获取path参数
   *
   * @param key 键
   * @return value
   */
  public String getPathVariable(String key) {
    return (String) getAttribute(PATH_VARS + key);
  }

  /**
   * 根据key获取header值 最多返回第一个值
   *
   * @param key 键
   * @return value
   */
  public String getHeader(String key) {
    return HttpHeaders.getHeader(key, headers);
  }

  /**
   * 根据key获取header数组
   *
   * @param key 键
   * @return values
   */
  public String[] getHeaders(String key) {
    return HttpHeaders.getHeaders(key, headers);
  }

  /**
   * 根据key获取cookie
   *
   * @param key 键
   * @return cookie
   */
  public Cookie getCookie(String key) {
    return cookies.get(key);
  }

  /**
   * 根据key获取cookie值
   *
   * @param key 键
   * @return value of cookie
   */
  public String getCookieValue(String key) {
    Cookie cookie = getCookie(key);
    if (cookie != null) {
      return cookie.getValue();
    }
    return null;
  }

  /**
   * 获取上传文件对象
   *
   * @param key 键
   * @return item
   */
  public MultipartItem getMultipartItem(String key) {
    if (isMultipart()) {
      return multipart.getData().get(key);
    }
    return null;
  }

  /**
   * 添加属性
   *
   * @param key 键
   * @param value 值
   * @return request
   */
  public Request addAttribute(String key, Object value) {
    this.attributes.put(key, value);
    return this;
  }

  /**
   * 添加多属性
   *
   * @param attrs 属性
   * @return request
   */
  public Request addAttribute(Map<String, Object> attrs) {
    this.attributes.putAll(attrs);
    return this;
  }

  /**
   * 获取属性
   *
   * @param key 键
   * @return value
   */
  public Object getAttribute(String key) {
    return this.attributes.get(key);
  }

  /**
   * 删除属性
   *
   * @param key 键
   * @return 删除的value
   */
  public Object removeAttribute(String key) {
    return this.attributes.remove(key);
  }

  /**
   * 设置当前请求异常
   *
   * @param exception 异常
   */
  public void setException(Exception exception) {
    this.exception = exception;
  }

  /**
   * 是否为ajax请求
   *
   * @return true为ajax请求
   */
  public boolean isAjax() {
    return Objects.equals(getHeader(HttpHeaders.X_REQUESTED_WITH), HttpHeaders.XML_HTTP_REQUEST);
  }

  /**
   * 是否为静态资源请求
   *
   * @return true为静态资源请求
   */
  public boolean isStatic() {
    return routeHandler instanceof StaticRouteHandler;
  }

}
