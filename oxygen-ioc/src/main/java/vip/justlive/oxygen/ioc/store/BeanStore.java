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
package vip.justlive.oxygen.ioc.store;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import vip.justlive.oxygen.core.Order;

/**
 * bean store
 *
 * @author wubo
 * @since 2.0.0
 */
public interface BeanStore extends Order {

  /**
   * 添加bean，名称默认为class name
   *
   * @param bean bean instance
   */
  default void addBean(Object bean) {
    addBean(bean.getClass().getSimpleName(), bean);
  }

  /**
   * 添加bean
   *
   * @param name name of bean
   * @param bean bean instance
   */
  default void addBean(String name, Object bean) {
    addBean(name, bean, Integer.MAX_VALUE);
  }

  /**
   * 添加bean，带优先级
   *
   * @param name name of bean
   * @param bean bean instance
   * @param order order of bean
   */
  void addBean(String name, Object bean, int order);

  /**
   * 根据name获取bean
   *
   * @param name name of bean
   * @return bean
   */
  Object getBean(String name);

  /**
   * 获取bean
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return bean
   */
  <T> T getBean(Class<T> clazz);

  /**
   * 根据name和类型获取bean
   *
   * @param name name of bean
   * @param clazz 类型
   * @param <T> 泛型
   * @return bean
   */
  <T> T getBean(String name, Class<T> clazz);

  /**
   * 获取类型
   *
   * @param clazz 类型
   * @return bean
   */
  Map<String, Object> getBeanMap(Class<?> clazz);

  /**
   * 获取类型
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return bean
   */
  <T> Map<String, T> getCastBeanMap(Class<T> clazz);

  /**
   * 获取beans
   *
   * @return beans
   */
  Collection<Object> getBeans();

  /**
   * 获取bean names
   *
   * @return bean names
   */
  Set<String> getBeanNames();

  /**
   * clean beans
   */
  void clean();
}
