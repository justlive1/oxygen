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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.HttpHeaders;
import vip.justlive.oxygen.core.util.Strings;
import vip.justlive.oxygen.web.result.Result;

/**
 * Response
 *
 * @author wubo
 */
@Data
public class Response implements Serializable {

  public static final String ORIGINAL_RESPONSE = "_ORIGINAL_RESPONSE";

  private static final long serialVersionUID = 1L;
  private static final ThreadLocal<Response> LOCAL = new ThreadLocal<>();

  private final Request request;
  /**
   * cookies
   */
  private final Map<String, Cookie> cookies = new HashMap<>(4);
  /**
   * headers
   */
  private final Map<String, String> headers = new HashMap<>(4);
  /**
   * 返回码
   */
  private int status = 200;
  /**
   * 编码
   */
  private String encoding = StandardCharsets.UTF_8.name();
  /**
   * contentType
   */
  private String contentType;
  /**
   * out
   */
  private transient ByteArrayOutputStream out = new ByteArrayOutputStream();
  /**
   * result
   */
  private transient Result result;

  public Response(Request request) {
    this.request = request;
    setHeader(HttpHeaders.SERVER, Bootstrap.version());
  }

  /**
   * 当前请求的response
   *
   * @return response
   */
  public static Response current() {
    return LOCAL.get();
  }

  /**
   * 清除response数据
   */
  public static void clear() {
    LOCAL.remove();
  }

  /**
   * 设置到当前线存储
   */
  public void local() {
    LOCAL.set(this);
  }

  /**
   * 设置cookie
   *
   * @param name name of cookie
   * @param value value
   */
  public void setCookie(String name, String value) {
    setCookie(name, value, null);
  }

  /**
   * 设置cookie
   *
   * @param name name of cookie
   * @param value value
   * @param maxAge max age
   */
  public void setCookie(String name, String value, Integer maxAge) {
    setCookie(name, value, null, Strings.SLASH, maxAge, false);
  }

  /**
   * 设置cookie
   *
   * @param name name of cookie
   * @param value value
   * @param maxAge max age
   * @param secure secure
   */
  public void setCookie(String name, String value, Integer maxAge, boolean secure) {
    setCookie(name, value, null, Strings.SLASH, maxAge, secure);
  }

  /**
   * 设置cookie
   *
   * @param name name of cookie
   * @param value value
   * @param domain domain
   * @param path path
   * @param maxAge max age
   * @param secure secure
   */
  public void setCookie(String name, String value, String domain, String path, Integer maxAge,
      boolean secure) {
    Cookie cookie = cookies.computeIfAbsent(name, k -> new Cookie());
    cookie.setName(name);
    cookie.setValue(value);
    cookie.setDomain(domain);
    cookie.setPath(path);
    cookie.setMaxAge(maxAge);
    cookie.setSecure(secure);
  }

  /**
   * 删除cookie
   *
   * @param name name of cookie
   */
  public void removeCookie(String name) {
    cookies.remove(name);
  }

  /**
   * 设置header
   *
   * @param name name of header
   * @param value value
   */
  public void setHeader(String name, String value) {
    headers.put(name, value);
  }

  /**
   * 获取header
   *
   * @param name name of header
   * @return value
   */
  public String getHeader(String name) {
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(name)) {
        return entry.getValue();
      }
    }
    return null;
  }

  /**
   * 删除header
   *
   * @param name name of header
   */
  public void removeHeader(String name) {
    headers.remove(name);
  }

  /**
   * 写数据
   *
   * @param data 数据
   */
  public void write(String data) {
    if (data == null) {
      return;
    }
    try {
      out.write(data.getBytes(Charset.forName(encoding)));
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  /**
   * 写入文本
   *
   * @param data 数据
   */
  public void text(String data) {
    setContentType(HttpHeaders.TEXT_PLAIN);
    write(data);
  }

  /**
   * 写入html
   *
   * @param data 数据
   */
  public void html(String data) {
    setContentType(HttpHeaders.TEXT_HTML);
    write(data);
  }

  /**
   * 写入json
   *
   * @param data 数据
   */
  public void json(Object data) {
    setResult(Result.json(data));
  }

  /**
   * 模板渲染
   *
   * @param path 模板路径
   */
  public void template(String path) {
    setResult(Result.view(path));
  }

  /**
   * 模板渲染
   *
   * @param path 模板路径
   * @param attrs 渲染参数
   */
  public void template(String path, Map<String, Object> attrs) {
    setResult(Result.view(path).addAttributes(attrs));
  }

  /**
   * 重定向
   *
   * @param url 地址
   */
  public void redirect(String url) {
    setResult(Result.redirect(url));
  }
}
