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
package vip.justlive.oxygen.core.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.scan.ClassScannerPlugin;

/**
 * IocPlugin
 * <br>
 * 当前只支持构造方法注入
 * <br>
 * 原因: 易于切换不同ioc实现
 *
 * @author wubo
 */
@Slf4j
public class IocPlugin implements Plugin {

  private BeanProxy beanProxy;

  @Override
  public int order() {
    return Integer.MIN_VALUE + 300;
  }

  @Override
  public void start() {
    beanProxy = ClassUtils.generateBeanProxy();
    log.info("init bean proxy of class [{}]", beanProxy.getClass());
    ioc();
  }

  @Override
  public void stop() {
    Singleton.clear();
  }

  private void ioc() {
    List<ClassInfo> remains = new ArrayList<>();
    for (Class<?> clazz : ClassScannerPlugin.getTypesAnnotatedWith(Bean.class)) {
      Bean bean;
      if (clazz.isInterface() || (bean = ClassUtils.getAnnotation(clazz, Bean.class)) == null) {
        continue;
      }
      String beanName = bean.value();
      if (beanName.length() == 0) {
        beanName = clazz.getName();
      }
      tryInstance(clazz, beanName, bean.order(), remains);
    }
    int count = remains.size();
    while (count > 0) {
      remains.removeIf(this::reIoc);
      if (count == remains.size()) {
        if (log.isDebugEnabled()) {
          log.debug("Is there an unresolvable circular reference? {}", remains);
        }
        throw Exceptions.fail("Is there an unresolvable circular reference?");
      }
      count = remains.size();
    }
  }

  private boolean tryInstance(ClassInfo classInfo) {
    Constructor<?> constructor = classInfo.constructor;
    if (constructor != null) {
      Object bean = dependencyInstance(classInfo.type, constructor);
      if (bean == null) {
        return false;
      }
      ConfigFactory.load(bean);
      Singleton.set(classInfo.name, bean, classInfo.order);
      return true;
    }
    Object bean = beanProxy.proxy(classInfo.type);
    ConfigFactory.load(bean);
    Singleton.set(classInfo.name, bean, classInfo.order);
    return true;
  }

  private Constructor<?> getConstructor(Class<?> clazz) {
    Constructor<?>[] constructors = clazz.getConstructors();
    Constructor<?> constructor;
    if (constructors.length == 1) {
      constructor = constructors[0];
    } else {
      constructor = ClassUtils.getConstructorAnnotatedWith(constructors, Inject.class);
    }
    return constructor;
  }

  private Object dependencyInstance(Class<?> clazz, Constructor<?> constructor) {
    Parameter[] params = constructor.getParameters();
    Object[] args = new Object[params.length];
    for (int i = 0; i < params.length; i++) {
      if (params[i].isAnnotationPresent(Named.class)) {
        args[i] = Singleton.get(params[i].getAnnotation(Named.class).value());
      } else {
        args[i] = Singleton.get(params[i].getType());
      }
      if (args[i] == null) {
        return null;
      }
    }
    return beanProxy.proxy(clazz, args);
  }

  private void tryInstance(Class<?> clazz, String beanName, int order, List<ClassInfo> remains) {
    ClassInfo classInfo = new ClassInfo(beanName, clazz, getConstructor(clazz), order);
    if (!tryInstance(classInfo)) {
      remains.add(classInfo);
    }
  }

  private boolean reIoc(ClassInfo classInfo) {
    Object bean = dependencyInstance(classInfo.type, classInfo.constructor);
    if (bean != null) {
      ConfigFactory.load(bean);
      Singleton.set(classInfo.name, bean, classInfo.order);
      return true;
    }
    return false;
  }

  @RequiredArgsConstructor
  private static class ClassInfo {

    final String name;
    final Class<?> type;
    final Constructor<?> constructor;
    final int order;

    @Override
    public String toString() {
      return String.format("[name=%s, %s, %s]", name, type, constructor);
    }
  }
}
