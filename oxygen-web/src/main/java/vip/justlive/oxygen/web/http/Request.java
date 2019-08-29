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
package vip.justlive.oxygen.web.http;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.web.router.Route;

/**
 * Request
 *
 * @author wubo
 */
@Getter
@ToString
@EqualsAndHashCode
public class Request implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final ThreadLocal<Request> LOCAL = new ThreadLocal<>();
  private static final String[] EMPTY = new String[0];

  private final transient HttpServletRequest originalRequest;
  /**
   * 主机名
   */
  String host;
  /**
   * 请求路径 去除host:port
   */
  String path;
  /**
   * 容器路径
   */
  String contextPath;
  /**
   * 请求全路径 http://host:port/path?queryString
   */
  String url;
  /**
   * 请求类型
   */
  String method;
  /**
   * 客户端ip
   */
  String remoteAddress;
  /**
   * 端口
   */
  Integer port;
  /**
   * 内容类型
   */
  String contentType = "text/html";
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

  transient Route route;
  /**
   * 异常
   */
  transient Exception exception;
  /**
   * query params
   */
  private Map<String, String[]> params;
  /**
   * 请求路径参数
   */
  private Map<String, String> pathVariables;
  /**
   * headers
   */
  private Map<String, String[]> headers;
  /**
   * cookies
   */
  private Map<String, Cookie> cookies;


  Request(HttpServletRequest originalRequest) {
    this.originalRequest = originalRequest;
  }

  /**
   * 设置线程值Request
   *
   * @param originalRequest 原始request
   * @return request
   */
  public static Request set(HttpServletRequest originalRequest) {
    Request request = new Request(originalRequest);
    request.params = new HashMap<>(8);
    request.pathVariables = new HashMap<>(2);
    request.cookies = new HashMap<>(4);
    request.headers = new HashMap<>(4);
    LOCAL.set(request);
    return request;
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
    if (params != null) {
      String[] values = params.get(key);
      if (values != null) {
        return values;
      }
    }
    return EMPTY;
  }

  /**
   * 根据key获取path参数
   *
   * @param key 键
   * @return value
   */
  public String getPathVariable(String key) {
    return pathVariables.get(key);
  }

  /**
   * 根据key获取header值 最多返回第一个值
   *
   * @param key 键
   * @return value
   */
  public String getHeader(String key) {
    String[] values = getHeaders(key);
    if (values.length > 0) {
      return values[0];
    }
    return null;
  }

  /**
   * 根据key获取header数组
   *
   * @param key 键
   * @return values
   */
  public String[] getHeaders(String key) {
    if (headers != null) {
      for (Map.Entry<String, String[]> entry : headers.entrySet()) {
        if (entry.getKey().equalsIgnoreCase(key)) {
          return entry.getValue();
        }
      }
    }
    return EMPTY;
  }

  /**
   * 根据key获取cookie
   *
   * @param key 键
   * @return cookie
   */
  public Cookie getCookie(String key) {
    if (cookies != null) {
      return cookies.get(key);
    }
    return null;
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
    return Objects.equals(getHeader(Constants.X_REQUESTED_WITH), Constants.XML_HTTP_REQUEST);
  }

  /**
   * 设置route
   *
   * @param route route
   */
  public void setRoute(Route route) {
    this.route = route;
  }
}
