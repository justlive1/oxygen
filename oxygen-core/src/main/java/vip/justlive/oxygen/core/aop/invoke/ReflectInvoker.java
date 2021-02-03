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
package vip.justlive.oxygen.core.aop.invoke;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.util.base.ClassUtils;

/**
 * 原生反射调用
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class ReflectInvoker implements Invoker {

  private final Object target;
  private final Method method;

  @Override
  public Object invoke() {
    return ClassUtils.methodInvoke(method, target);
  }

  @Override
  public Object invoke(Object[] args) {
    return ClassUtils.methodInvoke(method, target, args);
  }
}
