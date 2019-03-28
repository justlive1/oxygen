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
package vip.justlive.oxygen.ioc.store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * bean存储
 *
 * @author wubo
 * @since 2.0.0
 */
public class DefaultBeanStore implements BeanStore {

  private Set<Object> beans = new HashSet<>(32);
  private ConcurrentMap<Class<?>, List<BeanInfo>> beanClassMap = new ConcurrentHashMap<>(32);
  private ConcurrentMap<String, Object> beanNameMap = new ConcurrentHashMap<>(32);

  @Override
  public void addBean(String name, Object bean, int order) {
    if (beans.add(bean)) {
      Object oldVal = beanNameMap.putIfAbsent(name, bean);
      if (oldVal != null) {
        throw Exceptions.fail(String.format("[%s]名称[%s]已被[%s]占用", bean, name, oldVal));
      }
      BeanInfo beanInfo = new BeanInfo(name, bean, order);
      margeSuperClass(beanInfo);
    }
  }

  @Override
  public Object getBean(String name) {
    return beanNameMap.get(name);
  }

  @Override
  public <T> T getBean(Class<T> clazz) {
    List<BeanInfo> beanInfos = beanClassMap.get(clazz);
    if (beanInfos == null) {
      return null;
    }
    Collections.sort(beanInfos);
    BeanInfo beanInfo = beanInfos.get(0);
    if (beanInfos.size() > 1 && beanInfo.getOrder() == Integer.MAX_VALUE) {
      throw Exceptions.fail(String.format("存在多个该类型的bean [%s]", beanInfos));
    }
    return clazz.cast(beanInfo.getBean());
  }

  @Override
  public <T> T getBean(String name, Class<T> clazz) {
    return clazz.cast(getBean(name));
  }

  @Override
  public Map<String, Object> getBeanMap(Class<?> clazz) {
    List<BeanInfo> beanInfos = beanClassMap.get(clazz);
    if (beanInfos == null) {
      return null;
    }
    Map<String, Object> map = new HashMap<>(beanInfos.size());
    beanInfos.forEach(beanInfo -> map.put(beanInfo.getName(), beanInfo.getBean()));
    return map;
  }

  @Override
  public <T> Map<String, T> getCastBeanMap(Class<T> clazz) {
    List<BeanInfo> beanInfos = beanClassMap.get(clazz);
    if (beanInfos == null) {
      return null;
    }
    Map<String, T> map = new HashMap<>(beanInfos.size());
    beanInfos.forEach(beanInfo -> map.put(beanInfo.getName(), clazz.cast(beanInfo.getBean())));
    return map;
  }

  @Override
  public Collection<Object> getBeans() {
    return Collections.unmodifiableCollection(beans);
  }

  @Override
  public Set<String> getBeanNames() {
    return beanNameMap.keySet();
  }

  @Override
  public void clean() {
    beanClassMap.clear();
    beanNameMap.clear();
    beans.clear();
  }

  private void setBean(Class<?> clazz, BeanInfo beanInfo) {
    beanClassMap.computeIfAbsent(clazz, k -> new LinkedList<>()).add(beanInfo);
  }

  private void margeSuperClass(BeanInfo beanInfo) {
    Class<?> clazz = beanInfo.getBean().getClass();
    do {
      setBean(clazz, beanInfo);
      mergeInterface(clazz, beanInfo);
      clazz = clazz.getSuperclass();
    } while (clazz != null && clazz != Object.class);
  }

  private void mergeInterface(Class<?> clazz, BeanInfo beanInfo) {
    Class<?>[] interfaces = clazz.getInterfaces();
    for (Class<?> inter : interfaces) {
      if (!inter.getName().startsWith("java")) {
        setBean(inter, beanInfo);
      }
    }
  }

}
