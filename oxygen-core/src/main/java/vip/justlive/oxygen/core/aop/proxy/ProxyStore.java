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
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.aop.Aspect.TYPE;
import vip.justlive.oxygen.core.aop.interceptor.Interceptor;
import vip.justlive.oxygen.core.util.base.ClassUtils;

/**
 * proxy interceptor store
 *
 * @author wubo
 * @since 2.0.0
 */
@UtilityClass
public class ProxyStore {

  final List<Object> PROXIES = new LinkedList<>();
  private final Map<TYPE, List<Interceptor>> INTERCEPTORS = new ConcurrentHashMap<>(4, 1);
  private final Map<TYPE, Map<Method, List<Interceptor>>> STORE = new EnumMap<>(TYPE.class);
  private volatile boolean ready = false;

  static {
    for (TYPE type : TYPE.values()) {
      INTERCEPTORS.put(type, new LinkedList<>());
      STORE.put(type, new HashMap<>());
    }
  }

  /**
   * 添加拦截
   *
   * @param type 拦截类型
   * @param interceptors 拦截器
   */
  public void addInterceptor(TYPE type, Interceptor... interceptors) {
    if (interceptors == null || interceptors.length == 0) {
      return;
    }
    ready = false;
    for (Interceptor interceptor : interceptors) {
      INTERCEPTORS.get(type).add(interceptor);
    }
  }

  /**
   * 初始化
   */
  public void init() {
    if (ready) {
      return;
    }
    STORE.forEach((k, v) -> v.clear());
    PROXIES.forEach(bean -> Stream.of(ClassUtils.getActualClass(bean.getClass()).getMethods())
        .forEach(ProxyStore::handleMethod));
    STORE.forEach((k, v) -> v.forEach((method, interceptors) -> Collections.sort(interceptors)));
    ready = true;
  }

  private void handleMethod(Method method) {
    INTERCEPTORS.forEach((k, v) -> v.forEach(interceptor -> {
      if (interceptor.match(method)) {
        addInterceptor(k, method, interceptor);
      }
    }));
  }


  /**
   * 添加拦截
   *
   * @param type 拦截类型
   * @param method 方法
   * @param interceptor 拦截器
   */
  public void addInterceptor(TYPE type, Method method, Interceptor interceptor) {
    STORE.get(type).computeIfAbsent(method, k -> new LinkedList<>()).add(interceptor);
    ready = false;
  }

  /**
   * 添加拦截
   *
   * @param type 拦截类型
   * @param method 方法
   * @param interceptors 拦截器
   */
  public void addInterceptors(TYPE type, Method method, List<Interceptor> interceptors) {
    STORE.get(type).computeIfAbsent(method, k -> new LinkedList<>()).addAll(interceptors);
    ready = false;
  }

  /**
   * 根据方法获取拦截器
   *
   * @param type 拦截类型
   * @param method 方法
   * @return interceptors
   */
  public List<Interceptor> get(TYPE type, Method method) {
    if (!ready) {
      init();
    }
    return STORE.get(type).get(method);
  }

  /**
   * clean
   */
  public void clear() {
    STORE.clear();
    PROXIES.clear();
  }

}