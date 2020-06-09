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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;

/**
 * 单例对象管理器
 *
 * @author wubo
 */
@UtilityClass
public class Singleton {

  private final Set<Object> BEANS = new HashSet<>(16);
  private final Map<Class<?>, List<BeanInfo>> CLASS_MAP = new ConcurrentHashMap<>(8);
  private final Map<String, Object> NAME_MAP = new ConcurrentHashMap<>(8);

  public void set(Object bean) {
    if (bean == null) {
      return;
    }
    set(ClassUtils.getActualClass(bean.getClass()).getName(), bean, Integer.MAX_VALUE);
  }

  /**
   * 设置单例对象
   *
   * @param name 对象名称
   * @param bean 对象实例
   * @param order 优先级
   */
  public void set(String name, Object bean, int order) {
    if (bean == null) {
      return;
    }
    if (BEANS.add(bean)) {
      Object oldVal = NAME_MAP.putIfAbsent(name, bean);
      if (oldVal != null) {
        throw Exceptions.fail(String
            .format("Bean name [%s] for [%s] conflicts with existing [%s]", bean, name, oldVal));
      }
      margeInterfaces(new BeanInfo(name, bean, order));
    }
  }

  /**
   * 根据名称获取对象实例
   *
   * @param name 名称
   * @return bean
   */
  public Object get(String name) {
    return NAME_MAP.get(name);
  }

  /**
   * 根据类型获取对象
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return bean
   */
  public <T> T get(Class<T> clazz) {
    List<BeanInfo> beanInfos = CLASS_MAP.get(clazz);
    if (beanInfos == null) {
      return null;
    }
    BeanInfo beanInfo = beanInfos.get(0);
    if (beanInfos.size() > 1 && beanInfo.getOrder() == beanInfos.get(1).getOrder()) {
      throw Exceptions.fail(String.format("multi beans found with same priority [%s]", beanInfos));
    }
    return clazz.cast(beanInfo.getBean());
  }

  /**
   * 根据名称和类型获取对象实例
   *
   * @param name 名称
   * @param clazz 类型
   * @param <T> 泛型
   * @return bean
   */
  public <T> T get(String name, Class<T> clazz) {
    return clazz.cast(get(name));
  }

  /**
   * 获取相同类型的对象集合
   *
   * @param clazz 类型
   * @return bean map
   */
  public Map<String, Object> getMap(Class<?> clazz) {
    List<BeanInfo> beanInfos = CLASS_MAP.get(clazz);
    if (beanInfos == null) {
      return Collections.emptyMap();
    }
    return beanInfos.stream().collect(Collectors.toMap(BeanInfo::getName, BeanInfo::getBean));
  }

  /**
   * 获取相同类型的对象集合，类型转换
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return bean map
   */
  public <T> Map<String, T> getCastMap(Class<T> clazz) {
    List<BeanInfo> beanInfos = CLASS_MAP.get(clazz);
    if (beanInfos == null) {
      return Collections.emptyMap();
    }
    return beanInfos.stream()
        .collect(Collectors.toMap(BeanInfo::getName, info -> clazz.cast(info.getBean())));
  }

  /**
   * 获取相同类型的对象集合
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return bean list
   */
  public <T> List<T> getList(Class<T> clazz) {
    List<BeanInfo> beanInfos = CLASS_MAP.get(clazz);
    if (beanInfos == null) {
      return Collections.emptyList();
    }
    return beanInfos.stream().map(info -> clazz.cast(info.getBean())).collect(Collectors.toList());
  }

  /**
   * 获取所有对象
   *
   * @return beans
   */
  public Collection<Object> getAll() {
    return Collections.unmodifiableCollection(BEANS);
  }

  /**
   * 获取对象名称
   *
   * @return names
   */
  public Set<String> names() {
    return Collections.unmodifiableSet(NAME_MAP.keySet());
  }

  /**
   * 清空实例数据
   */
  public void clear() {
    CLASS_MAP.clear();
    NAME_MAP.clear();
    BEANS.clear();
  }

  private void set(Class<?> clazz, BeanInfo beanInfo) {
    List<BeanInfo> list = CLASS_MAP.computeIfAbsent(clazz, k -> new LinkedList<>());
    if (list.contains(beanInfo)) {
      return;
    }
    list.add(beanInfo);
    Collections.sort(list);
  }

  private void margeInterfaces(BeanInfo beanInfo) {
    Class<?> clazz = beanInfo.getType();
    do {
      set(clazz, beanInfo);
      mergeInterface(clazz, beanInfo);
      clazz = clazz.getSuperclass();
    } while (clazz != null && clazz != Object.class);
  }

  private void mergeInterface(Class<?> clazz, BeanInfo beanInfo) {
    Class<?>[] interfaces = clazz.getInterfaces();
    for (Class<?> inter : interfaces) {
      if (ClassUtils.isJavaInternalType(inter)) {
        continue;
      }
      set(inter, beanInfo);
    }
  }
}
