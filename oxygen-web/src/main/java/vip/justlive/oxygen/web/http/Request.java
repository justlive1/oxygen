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
package vip.justlive.oxygen.web.http;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import vip.justlive.oxygen.web.mapping.Action;

/**
 * Request
 *
 * @author wubo
 */
@Data
public class Request implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final ThreadLocal<Request> LOCAL = new ThreadLocal<>();
  private static final String[] EMPTY = new String[0];

  private final transient HttpServletRequest originalRequest;
  private final transient Action action;

  /**
   * 主机名
   */
  private String host;
  /**
   * 请求路径 去除host:port
   */
  private String path;
  /**
   * 容器路径
   */
  private String contentPath;
  /**
   * 请求全路径 http://host:port/path?queryString
   */
  private String url;
  /**
   * 请求类型
   */
  private String method;
  /**
   * 客户端ip
   */
  private String remoteAddress;
  /**
   * 端口
   */
  private Integer port;
  /**
   * 内容类型
   */
  private String contentType = "text/html";
  /**
   * 字符集编码
   */
  private String encoding = StandardCharsets.UTF_8.name();
  /**
   * queryString
   */
  private String queryString;

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

  /**
   * multipart
   */
  private Multipart multipart;

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
      String[] values = headers.get(key.toLowerCase());
      if (values != null) {
        return values;
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
   * 设置线程值Request
   *
   * @param originalRequest 原始request
   * @param action 执行逻辑
   */
  public static void set(HttpServletRequest originalRequest, Action action) {
    Request request = new Request(originalRequest, action);
    request.params = new HashMap<>(8);
    request.pathVariables = new HashMap<>(2);
    request.cookies = new HashMap<>(4);
    request.headers = new HashMap<>(4);
    LOCAL.set(request);
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
}
