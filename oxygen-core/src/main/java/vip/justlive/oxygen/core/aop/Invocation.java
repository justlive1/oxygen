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
package vip.justlive.oxygen.core.aop;

import java.lang.reflect.Method;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.aop.Aspect.TYPE;
import vip.justlive.oxygen.core.aop.interceptor.Interceptor;
import vip.justlive.oxygen.core.aop.proxy.ProxyStore;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * aop调用封装
 *
 * @author wubo
 */
@Data
@Slf4j
@RequiredArgsConstructor
public final class Invocation {

  private final Object target;
  private final Method method;
  private final Object[] args;
  private Object returnValue;

  public void intercept(Callback callback) {
    boolean interrupted = doIntercept(TYPE.BEFORE, method);
    try {
      if (interrupted && log.isDebugEnabled()) {
        log.debug("aop intercepted and return an updated value before invoke super method [{}]",
            method);
      } else if (!interrupted) {
        returnValue = callback.apply(this);
      }
    } catch (Throwable e) {
      doIntercept(TYPE.CATCHING, method);
      throw Exceptions.wrap(e);
    }
    doIntercept(TYPE.AFTER, method);
  }

  private boolean doIntercept(TYPE type, Method method) {
    List<Interceptor> interceptors = ProxyStore.get(type, method);
    if (interceptors == null) {
      return false;
    }
    for (int index = 0, len = interceptors.size(); index < len; index++) {
      if (!interceptors.get(index).intercept(this)) {
        if (log.isDebugEnabled()) {
          log.debug("aop intercepted, total:{}, current: {}", interceptors.size(), index);
        }
        return true;
      }
    }
    return false;
  }
}
