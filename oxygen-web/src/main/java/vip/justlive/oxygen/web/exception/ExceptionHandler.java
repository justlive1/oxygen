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
package vip.justlive.oxygen.web.exception;

import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * 异常处理
 *
 * @author wubo
 */
public interface ExceptionHandler {

  /**
   * 异常处理
   *
   * @param ctx 上下文
   * @param e 异常
   * @param status 状态码
   */
  void handle(RoutingContext ctx, Exception e, int status);

  /**
   * 无法处理的异常
   *
   * @param ctx 上下文
   * @param e 异常
   */
  void error(RoutingContext ctx, Throwable e);
}
