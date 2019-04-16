/*
 *  Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */
package vip.justlive.oxygen.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.ServiceLoaderUtils;
import vip.justlive.oxygen.ioc.annotation.Bean;
import vip.justlive.oxygen.ioc.annotation.Inject;
import vip.justlive.oxygen.ioc.annotation.Named;
import vip.justlive.oxygen.ioc.store.BeanProxy;
import vip.justlive.oxygen.ioc.store.BeanStore;
import vip.justlive.oxygen.ioc.store.DefaultBeanProxy;
import vip.justlive.oxygen.ioc.store.DefaultBeanStore;

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

  private static final BeanStore BEAN_STORE = new DefaultBeanStore();
  private static final BeanProxy BEAN_PROXY;

  static {
    List<BeanProxy> list = ServiceLoaderUtils.loadServices(BeanProxy.class);
    if (list.isEmpty()) {
      BEAN_PROXY = new DefaultBeanProxy();
    } else {
      Collections.sort(list);
      BEAN_PROXY = list.get(0);
    }
  }

  /**
   * 获取beanStore
   *
   * @return beanStore
   */
  public static BeanStore beanStore() {
    return BEAN_STORE;
  }

  /**
   * 获取beanProxy
   *
   * @return beanProxy
   */
  public static BeanProxy beanProxy() {
    return BEAN_PROXY;
  }

  /**
   * try instance
   *
   * @param clazz 类
   * @param beanName name of bean
   * @param order order of bean
   * @return instance successful if true
   */
  public static boolean tryInstance(Class<?> clazz, String beanName, int order) {
    Constructor<?> constructor = ClassUtils.getConstructorAnnotatedWith(clazz, Inject.class);
    if (constructor != null) {
      Object bean = dependencyInstance(clazz, constructor);
      if (bean == null) {
        return false;
      }
      ConfigFactory.load(bean);
      BEAN_STORE.addBean(beanName, bean, order);
      return true;
    }
    Object bean = BEAN_PROXY.proxy(clazz);
    ConfigFactory.load(bean);
    BEAN_STORE.addBean(beanName, bean, order);
    return true;
  }

  private static Object dependencyInstance(Class<?> clazz, Constructor<?> constructor) {
    Parameter[] params = constructor.getParameters();
    Object[] args = new Object[params.length];
    for (int i = 0; i < params.length; i++) {
      if (params[i].isAnnotationPresent(Named.class)) {
        args[i] = BEAN_STORE.getBean(params[i].getAnnotation(Named.class).value());
      } else {
        args[i] = BEAN_STORE.getBean(params[i].getType());
      }
      if (args[i] == null) {
        return null;
      }
    }
    return BEAN_PROXY.proxy(clazz, args);
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 10;
  }

  @Override
  public void start() {
    ioc();
  }

  @Override
  public void stop() {
    BEAN_STORE.clean();
  }

  private void ioc() {
    Set<Class<?>> beanClasses = ClassScannerPlugin.getTypesAnnotatedWith(Bean.class);
    Map<Class<?>, ClassInfo> iocMap = new HashMap<>(8);
    for (Class<?> clazz : beanClasses) {
      Bean bean = clazz.getAnnotation(Bean.class);
      String beanName = bean.value();
      if (beanName.length() == 0) {
        beanName = clazz.getName();
      }
      tryInstance(clazz, beanName, bean.order(), iocMap);
    }
    int count = iocMap.size();
    while (count > 0) {
      reIoc(iocMap);
      if (count == iocMap.size()) {
        if (log.isDebugEnabled()) {
          log.debug("ioc失败 出现循环依赖或缺失Bean fail-ioc-map {}", iocMap);
        }
        throw Exceptions.fail("发生循环依赖或者缺失Bean ");
      }
      count = iocMap.size();
    }
  }

  private void reIoc(Map<Class<?>, ClassInfo> iocMap) {
    Iterator<Entry<Class<?>, ClassInfo>> it = iocMap.entrySet().iterator();
    while (it.hasNext()) {
      Entry<Class<?>, ClassInfo> entry = it.next();
      ClassInfo classInfo = entry.getValue();
      Object bean = dependencyInstance(entry.getKey(), classInfo.constructor);
      if (bean != null) {
        ConfigFactory.load(bean);
        BEAN_STORE.addBean(classInfo.name, bean, classInfo.order);
        it.remove();
      }
    }
  }

  private void tryInstance(Class<?> clazz, String beanName, int order,
      Map<Class<?>, ClassInfo> iocMap) {
    if (!tryInstance(clazz, beanName, order)) {
      iocMap.put(clazz,
          new ClassInfo(beanName, ClassUtils.getConstructorAnnotatedWith(clazz, Inject.class),
              order));
    }
  }

  private static class ClassInfo {

    final String name;
    final Constructor<?> constructor;
    final int order;

    ClassInfo(String name, Constructor<?> constructor, int order) {
      this.name = name;
      this.constructor = constructor;
      this.order = order;
    }

    @Override
    public String toString() {
      return String.format("[name=%s, %s]", name, constructor);
    }
  }
}
