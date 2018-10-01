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
package vip.justlive.oxygen.jdbc.interceptor;

import java.util.List;

/**
 * jdbc拦截
 *
 * @author wubo
 */
public interface JdbcInterceptor {

  /**
   * 前置拦截
   *
   * @param sql sql
   * @param params 参数
   */
  default void before(String sql, List<Object> params) {
  }

  /**
   * 后置拦截
   *
   * @param sql sql
   * @param params 参数
   * @param result 转换结果
   */
  default void after(String sql, List<Object> params, Object result) {
  }

  /**
   * 异常拦截
   *
   * @param sql sql
   * @param params 参数
   * @param e 异常
   */
  default void onException(String sql, List<Object> params, Exception e) {
  }

  /**
   * finally拦截
   *
   * @param sql sql
   * @param params 参数
   * @param result 转换结果
   */
  default void onFinally(String sql, List<Object> params, Object result) {
  }
}
