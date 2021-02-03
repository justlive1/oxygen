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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import vip.justlive.oxygen.core.CoreConfigKeys;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.aop.Aspect.TYPE;
import vip.justlive.oxygen.core.aop.interceptor.AnnotationInterceptor;
import vip.justlive.oxygen.core.aop.interceptor.MethodInterceptor;
import vip.justlive.oxygen.core.aop.proxy.ProxyStore;
import vip.justlive.oxygen.core.bean.Singleton;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;

/**
 * aop插件
 *
 * @author wubo
 */
public class AopPlugin implements Plugin {

  @Override
  public int order() {
    return Integer.MIN_VALUE + 600;
  }

  @Override
  public void start() {
    if (!CoreConfigKeys.AOP_ENABLED.castValue(boolean.class)) {
      return;
    }
    Singleton.getAll().forEach(
        bean -> ClassUtils.getMethodsAnnotatedWith(bean.getClass(), Aspect.class)
            .forEach(method -> handleMethod(method, bean)));
    ProxyStore.init();
  }

  @Override
  public void stop() {
    if (CoreConfigKeys.AOP_ENABLED.castValue(boolean.class)) {
      ProxyStore.clear();
    }
  }

  private void handleMethod(Method method, Object bean) {
    Aspect aspect = method.getAnnotation(Aspect.class);
    Method realMethod;
    try {
      realMethod = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
    } catch (NoSuchMethodException e) {
      throw Exceptions.wrap(e);
    }
    AopWrapper wrapper = new AopWrapper(bean, realMethod, aspect.order(),
        ClassUtils.generateInvoker(bean, realMethod));
    for (TYPE type : aspect.type()) {
      if (aspect.annotation() != Annotation.class) {
        ProxyStore.addInterceptor(type, new AnnotationInterceptor(aspect.annotation(), wrapper));
      }
      if (aspect.method().length() > 0) {
        ProxyStore.addInterceptor(type, new MethodInterceptor(aspect.method(), wrapper));
      }
    }
  }
}
