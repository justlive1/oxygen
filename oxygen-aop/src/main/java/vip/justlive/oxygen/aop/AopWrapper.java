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
package vip.justlive.oxygen.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.Data;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

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
  private final FastMethod fastMethod;

  public AopWrapper(Object target, Method method, int order) {
    this.target = target;
    this.method = method;
    this.order = order;
    this.fastMethod = FastClass.create(target.getClass()).getMethod(method);
  }

  public Object invoke() throws InvocationTargetException {
    return fastMethod.invoke(target, new Object[0]);
  }

  public Object invoke(Invocation invocation) throws InvocationTargetException {
    return fastMethod.invoke(target, new Object[]{invocation});
  }

}
