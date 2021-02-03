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
package vip.justlive.oxygen.core.aop.proxy;

import java.lang.reflect.Method;
import vip.justlive.oxygen.core.aop.Invocation;
import vip.justlive.oxygen.core.aop.interceptor.Interceptor;

/**
 * @author wubo
 */
public class NoLogInterceptor implements Interceptor {

  @Override
  public boolean match(Method method) {
    return method.isAnnotationPresent(NoLog.class);
  }

  @Override
  public boolean intercept(Invocation invocation) {
    System.out.println("enter into no log interceptor ");
    NoLogService.ato.set(NoLogService.ato.get() * 10 + 2);
    return true;
  }
}
