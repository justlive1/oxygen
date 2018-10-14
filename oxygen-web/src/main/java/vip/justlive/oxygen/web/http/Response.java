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
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * Response
 *
 * @author wubo
 */
@Data
public class Response implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final ThreadLocal<Response> LOCAL = new ThreadLocal<>();

  private final transient HttpServletResponse originalResponse;

  /**
   * 编码
   */
  private String encoding = StandardCharsets.UTF_8.name();

  /**
   * contentType
   */
  private String contentType;

  /**
   * cookies
   */
  private Map<String, Cookie> cookies = new HashMap<>(4);

  /**
   * headers
   */
  private Map<String, String> headers = new HashMap<>(4);

  /**
   * 设置线程值response
   *
   * @param originalResponse 原始response
   */
  public static void set(HttpServletResponse originalResponse) {
    Response response = new Response(originalResponse);
    LOCAL.set(response);
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
    setCookie(name, value, null, Constants.ROOT_PATH, maxAge, false);
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
    setCookie(name, value, null, Constants.ROOT_PATH, maxAge, secure);
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
    Cookie cookie = cookies.get(name);
    if (cookie == null) {
      cookie = new Cookie();
      cookies.put(name, cookie);
    }
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
}
