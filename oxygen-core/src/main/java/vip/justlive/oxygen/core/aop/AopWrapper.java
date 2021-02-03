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
import lombok.Data;
import vip.justlive.oxygen.core.aop.invoke.Invoker;

/**
 * aop包装
 *
 * @author wubo
 */
@Data
public class AopWrapper {

  private final Object target;
  private final Method method;
  private final int order;
  private final Invoker invoke;

  public Object invoke() {
    return invoke.invoke();
  }

  public Object invoke(Invocation invocation) {
    return invoke.invoke(new Object[]{invocation});
  }
}
