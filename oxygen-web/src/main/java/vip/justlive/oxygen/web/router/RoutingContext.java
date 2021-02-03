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
package vip.justlive.oxygen.web.router;

import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;

/**
 * 路由上下文
 *
 * @author wubo
 */
public interface RoutingContext {

  /**
   * 获取 request
   *
   * @return request
   */
  Request request();

  /**
   * 获取response
   *
   * @return response
   */
  Response response();

  /**
   * 请求路径
   *
   * @return path
   */
  String requestPath();

  /**
   * param参数绑定
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return obj
   */
  <T> T bindParam(Class<T> clazz);

  /**
   * cookie参数绑定
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return obj
   */
  <T> T bindCookie(Class<T> clazz);

  /**
   * header参数绑定
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return obj
   */
  <T> T bindHeader(Class<T> clazz);

  /**
   * path参数绑定
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return obj
   */
  <T> T bindPathVariables(Class<T> clazz);
}
