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
package vip.justlive.oxygen.jdbc.interceptor;

import vip.justlive.oxygen.core.Order;

/**
 * jdbc拦截
 *
 * @author wubo
 */
public interface JdbcInterceptor extends Order {

  /**
   * 前置拦截
   *
   * @param ctx sql上下文
   */
  default void before(SqlCtx ctx) {
  }

  /**
   * 后置拦截
   *
   * @param ctx sql上下文
   * @param result 转换结果
   */
  default void after(SqlCtx ctx, Object result) {
  }

  /**
   * 异常拦截
   *
   * @param ctx sql上下文
   * @param e 异常
   */
  default void onException(SqlCtx ctx, Exception e) {
  }

  /**
   * finally拦截
   *
   * @param ctx sql上下文
   * @param result 转换结果
   */
  default void onFinally(SqlCtx ctx, Object result) {
  }
}
