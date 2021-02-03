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
package vip.justlive.oxygen.web.result;

import vip.justlive.oxygen.core.Order;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * 结果
 *
 * @author wubo
 */
public interface ResultHandler extends Order {

  /**
   * 是否支持
   *
   * @param result 结果
   * @return true为支持
   */
  boolean support(Result result);

  /**
   * 处理
   *
   * @param ctx 上下文
   * @param result 结果
   */
  void apply(RoutingContext ctx, Result result);
}
