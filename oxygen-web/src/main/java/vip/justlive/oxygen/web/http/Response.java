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
import javax.servlet.http.HttpServletResponse;
import lombok.Data;

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
}
