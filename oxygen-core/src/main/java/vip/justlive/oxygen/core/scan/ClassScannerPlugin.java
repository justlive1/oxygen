/*
 * Copyright (C) 2019 the original author or authors.
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
package vip.justlive.oxygen.core.scan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * 类扫描插件
 *
 * @author wubo
 */
public class ClassScannerPlugin implements Plugin {

  private static final Set<Class<?>> CLASSES = new HashSet<>();
  private static final Map<Class<? extends Annotation>, ClassStore> CACHE = new ConcurrentHashMap<>();

  /**
   * 根据注解获取类
   *
   * @param annotation 注解
   * @return classes
   */
  public static Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation) {
    ClassStore store = CACHE.computeIfAbsent(annotation, k -> new ClassStore());
    if (store.classes == null) {
      store.classes = new HashSet<>();
      for (Class<?> clazz : CLASSES) {
        if (ClassUtils.isAnnotationPresent(clazz, annotation)) {
          store.classes.add(clazz);
        }
      }
    }
    return store.classes;
  }

  /**
   * 根据注解获取方法
   *
   * @param annotation 注解
   * @return methods
   */
  public static Set<Method> getMethodsAnnotatedWith(final Class<? extends Annotation> annotation) {
    ClassStore store = CACHE.computeIfAbsent(annotation, k -> new ClassStore());
    if (store.methods == null) {
      store.methods = new HashSet<>();
      for (Class<?> clazz : CLASSES) {
        store.methods.addAll(ClassUtils.getMethodsAnnotatedWith(clazz, annotation));
      }
    }
    return store.methods;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE;
  }

  @Override
  public void start() {
    CoreConf config = ConfigFactory.load(CoreConf.class);
    ClassScanner scanner = new DefaultClassScanner();
    Set<String> pkgs = new HashSet<>(2);
    if (config.getClassScan() != null) {
      pkgs.addAll(Arrays.asList(config.getClassScan()));
    }
    pkgs.add("vip.justlive.oxygen");
    CLASSES.addAll(scanner.scan(pkgs.toArray(new String[0])));
  }

  @Override
  public void stop() {
    CLASSES.clear();
    CACHE.clear();
  }

  /**
   * class store
   */
  static class ClassStore {

    Set<Class<?>> classes;
    Set<Method> methods;
  }
}
