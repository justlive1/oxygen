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
package vip.justlive.oxygen.core.util.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import lombok.experimental.UtilityClass;

/**
 * service loader utils
 *
 * @author wubo
 */
@UtilityClass
public class ServiceLoaderUtils {

  /**
   * 加载服务
   *
   * @param clazz 类
   * @param <T> 泛型
   * @return service
   */
  public <T> T loadService(Class<T> clazz) {
    T service = loadServiceOrNull(clazz);
    if (service == null) {
      throw new IllegalStateException(
          "Cannot find META-INF/services/" + clazz.getName() + " on classpath");
    }
    return service;
  }

  /**
   * 加载服务
   *
   * @param clazz 类
   * @param <T> 泛型
   * @return service or null
   */
  public <T> T loadServiceOrNull(Class<T> clazz) {
    Collection<T> services = loadServices(clazz);
    if (services.isEmpty()) {
      return null;
    }
    return services.iterator().next();
  }

  /**
   * 加载服务列表
   *
   * @param clazz 类
   * @param <T> 泛型
   * @return services
   */
  public <T> List<T> loadServices(Class<T> clazz) {
    return loadServices(clazz, null);
  }

  /**
   * 加载服务列表
   *
   * @param clazz 类
   * @param loader 类加载器
   * @param <T> 泛型
   * @return services
   */
  public <T> List<T> loadServices(Class<T> clazz, ClassLoader loader) {
    List<T> list = new ArrayList<>();
    ServiceLoader<T> serviceLoader;
    if (loader != null) {
      serviceLoader = ServiceLoader.load(clazz, loader);
    } else {
      serviceLoader = ServiceLoader.load(clazz);
    }
    Iterator<T> it = serviceLoader.iterator();
    if (it.hasNext()) {
      it.forEachRemaining(list::add);
      return list;
    }
    if (loader == null) {
      // for tccl
      serviceLoader = ServiceLoader.load(clazz, ServiceLoaderUtils.class.getClassLoader());
      it = serviceLoader.iterator();
      if (it.hasNext()) {
        it.forEachRemaining(list::add);
        return list;
      }
    }
    return list;
  }
}
