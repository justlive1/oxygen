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
package vip.justlive.oxygen.web.router;

import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.ioc.IocPlugin;
import vip.justlive.oxygen.web.exception.ExceptionHandler;

/**
 * 路由处理器
 *
 * @author wubo
 */
@FunctionalInterface
public interface RouteHandler {

  /**
   * 处理404异常
   *
   * @param ctx 上下文
   */
  static void notFound(RoutingContext ctx) {
    IocPlugin.beanStore().getBean(ExceptionHandler.class)
        .handle(ctx, Exceptions.fail("No handle found"), 404);
  }

  /**
   * 可处理的异常
   *
   * @param ctx 上下文
   * @param e 异常
   */
  static void exception(RoutingContext ctx, Exception e) {
    IocPlugin.beanStore().getBean(ExceptionHandler.class).handle(ctx, e, 500);
  }

  /**
   * 无法处理的异常
   *
   * @param ctx 上下文
   * @param e 异常
   */
  static void error(RoutingContext ctx, Exception e) {
    IocPlugin.beanStore().getBean(ExceptionHandler.class).error(ctx, e);
  }

  /**
   * 处理
   *
   * @param ctx 上下文
   */
  void handle(RoutingContext ctx);

}
