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
package vip.justlive.oxygen.core.aop.interceptor;

import vip.justlive.oxygen.core.aop.AopWrapper;
import vip.justlive.oxygen.core.aop.Invocation;

/**
 * aop拦截
 *
 * @author wubo
 */
public abstract class AbstractInterceptor implements Interceptor {

  private final AopWrapper aopWrapper;

  AbstractInterceptor(AopWrapper aopWrapper) {
    this.aopWrapper = aopWrapper;
  }

  @Override
  public int order() {
    return aopWrapper.getOrder();
  }

  @Override
  public boolean intercept(Invocation invocation) {
    int count = aopWrapper.getMethod().getParameterCount();
    check(aopWrapper);
    Object result;
    if (count == 0) {
      result = aopWrapper.invoke();
    } else {
      result = aopWrapper.invoke(invocation);
    }
    Class<?> returnType = aopWrapper.getMethod().getReturnType();
    if (returnType == boolean.class) {
      return (boolean) result;
    }
    return true;
  }

  private void check(AopWrapper aopWrapper) {
    int count = aopWrapper.getMethod().getParameterCount();
    boolean b = (count == 1 && aopWrapper.getMethod().getParameterTypes()[0] != Invocation.class)
        || count > 1;
    if (b) {
      throw new IllegalArgumentException("argument can only be [Invocation]");
    }
    Class<?> returnType = aopWrapper.getMethod().getReturnType();
    if (returnType != Void.TYPE && returnType != boolean.class) {
      throw new IllegalArgumentException("return type can only be [void] or [boolean]");
    }
  }
}
