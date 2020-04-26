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
package vip.justlive.oxygen.aop.interceptor;

import java.lang.reflect.Method;
import vip.justlive.oxygen.aop.Invocation;
import vip.justlive.oxygen.core.Order;

/**
 * 拦截接口
 *
 * @author wubo
 */
public interface Interceptor extends Order {

  /**
   * 是否匹配
   *
   * @param method 方法
   * @return true则拦截
   */
  default boolean match(Method method) {
    return false;
  }

  /**
   * 拦截
   *
   * @param invocation 调用封装
   * @return true为继续执行下一个拦截
   */
  default boolean intercept(Invocation invocation) {
    return true;
  }

}
