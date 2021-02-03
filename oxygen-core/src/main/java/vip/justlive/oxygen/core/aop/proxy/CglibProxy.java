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
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import vip.justlive.oxygen.core.aop.Invocation;
import vip.justlive.oxygen.core.util.base.ClassUtils;

/**
 * cglib代理
 *
 * @author wubo
 */
public class CglibProxy implements MethodInterceptor {

  private static final CglibProxy CGLIB_PROXY = new CglibProxy();

  /**
   * 代理
   *
   * @param targetClass 目标
   * @param args 参数
   * @param <T> 泛型
   * @return 代理对象
   */
  public static <T> T proxy(Class<T> targetClass, Object... args) {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(targetClass);
    enhancer.setCallback(CGLIB_PROXY);
    Class<?>[] classes = ClassUtils.getConstructorArgsTypes(targetClass, args);
    T bean = targetClass.cast(enhancer.create(classes, args));
    ProxyStore.PROXIES.add(bean);
    return bean;
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) {
    Invocation invocation = new Invocation(obj, method, args);
    invocation.intercept(ivt -> methodProxy.invokeSuper(ivt.getTarget(), ivt.getArgs()));
    return invocation.getReturnValue();
  }
}
