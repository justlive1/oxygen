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
package vip.justlive.oxygen.core.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * cglib代理
 *
 * @author wubo
 */
public class CglibProxy implements MethodInterceptor {

  private static final CglibProxy CGLIB_PROXY = new CglibProxy();
  private static final Map<Method, List<Interceptor>> CACHE = new ConcurrentHashMap<>(16, 1);

  /**
   * 代理
   *
   * @param targetClass 目标
   * @param <T> 泛型
   * @return 代理对象
   */
  public static <T> T proxy(Class<T> targetClass) {
    return targetClass.cast(Enhancer.create(targetClass, CGLIB_PROXY));
  }

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
    Class<?>[] classes = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      classes[i] = args[i].getClass();
    }
    return targetClass.cast(enhancer.create(classes, args));
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy)
      throws Throwable {
    List<Interceptor> interceptors = CACHE.get(method);
    if (interceptors == null) {
      List<Interceptor> list = new ArrayList<>();
      parseInterceptor(method, list);
      CACHE.putIfAbsent(method, list);
    }
    interceptors = CACHE.get(method);
    Invocation invocation = new Invocation(obj, method, args);
    int index = 0;
    for (Interceptor interceptor : interceptors) {
      if (interceptor.before(invocation)) {
        index++;
      } else {
        break;
      }
    }
    try {
      invocation.setReturnValue(methodProxy.invokeSuper(obj, args));
    } catch (Exception e) {
      for (Interceptor interceptor : interceptors) {
        if (!interceptor.catching(invocation)) {
          break;
        }
      }
      throw e;
    }
    index--;
    for (; index >= 0; index--) {
      if (!interceptors.get(index).after(invocation)) {
        break;
      }
    }
    return invocation.getReturnValue();
  }

  private void parseInterceptor(Method method, List<Interceptor> list) {
    Annotation[] annotations = method.getAnnotations();
    for (Annotation annotation : annotations) {
      parseBeforeInterceptor(annotation, list);
      parseAfterInterceptor(annotation, list);
      parseCatchingInterceptor(annotation, list);
    }
  }

  private void parseBeforeInterceptor(Annotation annotation, List<Interceptor> list) {
    List<AopWrapper> aopWrapper = AopPlugin.getAopMethod(Before.class, annotation.annotationType());
    if (aopWrapper != null && !aopWrapper.isEmpty()) {
      list.add(new BeforeInterceptor(aopWrapper));
    }
  }

  private void parseAfterInterceptor(Annotation annotation, List<Interceptor> list) {
    List<AopWrapper> aopWrapper = AopPlugin.getAopMethod(After.class, annotation.annotationType());
    if (aopWrapper != null && !aopWrapper.isEmpty()) {
      list.add(new AfterInterceptor(aopWrapper));
    }
  }

  private void parseCatchingInterceptor(Annotation annotation, List<Interceptor> list) {
    List<AopWrapper> aopWrapper = AopPlugin
        .getAopMethod(Catching.class, annotation.annotationType());
    if (aopWrapper != null && !aopWrapper.isEmpty()) {
      list.add(new CatchingInterceptor(aopWrapper));
    }
  }

}
