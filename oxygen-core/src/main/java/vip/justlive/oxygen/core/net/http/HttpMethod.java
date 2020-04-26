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

package vip.justlive.oxygen.core.net.http;

import java.util.HashMap;
import java.util.Map;

/**
 * http方法枚举
 *
 * @author wubo
 */
public enum HttpMethod {
  /**
   * get
   */
  GET,
  /**
   * head
   */
  HEAD,
  /**
   * post
   */
  POST,
  /**
   * put
   */
  PUT,
  /**
   * delete
   */
  DELETE,
  /**
   * options
   */
  OPTIONS,
  /**
   * trace
   */
  TRACE,
  /**
   * patch
   */
  PATCH,
  /**
   * connect
   */
  CONNECT,
  /**
   * unknown
   */
  UNKNOWN;

  private static final Map<String, HttpMethod> METHODS = new HashMap<>(8, 1);

  static {
    for (HttpMethod method : HttpMethod.values()) {
      METHODS.put(method.name(), method);
    }
  }

  /**
   * 获取HttpMethod
   *
   * @param method 请求方法
   * @return HttpMethod
   */
  public static HttpMethod find(String method) {
    return METHODS.getOrDefault(method, UNKNOWN);
  }
}
