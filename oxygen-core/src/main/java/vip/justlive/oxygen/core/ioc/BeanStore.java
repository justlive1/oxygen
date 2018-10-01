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
package vip.justlive.oxygen.core.ioc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * bean存储
 *
 * @author wubo
 */
public class BeanStore {

  static final ConcurrentMap<Class<?>, ConcurrentMap<String, Object>> BEANS =
      new ConcurrentHashMap<>();

  static final Object EMPTY = new Object();

  BeanStore() {
  }

  /**
   * 根据类型获取bean
   *
   * @param clazz 类
   * @return bean
   */
  public static <T> T getBean(Class<T> clazz) {
    return getBean(clazz.getName(), clazz);
  }

  /**
   * 根据类型获取beanMap
   *
   * @param clazz 类
   * @param <T> 泛型
   * @return beanMap
   */
  public static <T> Map<String, T> getBeanMap(Class<T> clazz) {
    ConcurrentMap<String, Object> map = BEANS.get(clazz);
    if (map != null) {
      Map<String, T> result = new HashMap<>(map.size(), 1f);
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        result.put(entry.getKey(), clazz.cast(entry.getValue()));
      }
      return result;
    }
    return null;
  }


  /**
   * 根据类型和名称获取bean
   *
   * @param name beanId
   * @param clazz 类
   * @return bean
   */
  public static <T> T getBean(String name, Class<T> clazz) {
    ConcurrentMap<String, Object> map = BEANS.get(clazz);
    if (map != null) {
      Object val = map.get(name);
      if (val != EMPTY) {
        return clazz.cast(val);
      }
    }
    return null;
  }

  static void seize(Class<?> clazz) {
    ConcurrentMap<String, Object> map = BEANS.get(clazz);
    if (map == null) {
      BEANS.putIfAbsent(clazz, new ConcurrentHashMap<>(1, 1f));
    }
  }

  static <T> void seize(Class<T> clazz, String name) {
    seize(clazz);
    if (BEANS.get(clazz).putIfAbsent(name, EMPTY) != null) {
      throw new IllegalArgumentException(String.format("[%s] 名称已被定义", name));
    }
  }

  static <T> void putBean(String name, T bean) {
    Class<?> clazz = bean.getClass();
    seize(clazz);
    mergeSuperClass(name, clazz, bean);
  }

  static void mergeInterface(String name, Class<?> clazz, Object bean) {
    merge(clazz, bean);
    merge(clazz, bean, name);
    Class<?>[] interfaces = clazz.getInterfaces();
    for (Class<?> inter : interfaces) {
      if (!inter.getName().startsWith("java")) {
        seize(inter);
        Object local = BEANS.get(inter).putIfAbsent(name, bean);
        if (local != null && local != EMPTY) {
          throw new IllegalArgumentException(String.format("[%s] 名称已被定义", name));
        }
        merge(inter, bean);
      }
    }
  }

  static void mergeSuperClass(String name, Class<?> clazz, Object bean) {
    Class<?> supperClass = clazz;
    do {
      mergeInterface(name, supperClass, bean);
      supperClass = supperClass.getSuperclass();
    } while (supperClass != null && supperClass != Object.class);
  }

  static void merge(Class<?> clazz, Object bean) {
    ConcurrentMap<String, Object> map = BEANS.get(clazz);
    Object local = map.get(clazz.getName());
    if (local == null || local == EMPTY) {
      map.put(clazz.getName(), bean);
    }
  }

  static void merge(Class<?> clazz, Object bean, String name) {
    ConcurrentMap<String, Object> map = BEANS.get(clazz);
    Object local = map.get(name);
    if (local == null || local == EMPTY) {
      map.put(name, bean);
    }
  }

}
