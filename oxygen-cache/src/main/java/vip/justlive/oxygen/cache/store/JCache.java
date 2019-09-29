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
package vip.justlive.oxygen.cache.store;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * cache 调用入口
 *
 * @author wubo
 */
@UtilityClass
public class JCache {

  static Map<String, Cache> cacheImpls = new ConcurrentHashMap<>(4, 1);

  /**
   * 获取默认cache
   *
   * @return cache
   */
  public static Cache cache() {
    return cache(JCache.class.getSimpleName());
  }

  /**
   * 根据缓存名称获取cache
   *
   * @param name cache name
   * @return cache
   */
  public static Cache cache(String name) {
    if (!cacheImpls.containsKey(name)) {
      cacheImpls.putIfAbsent(name, createCache(name));
    }
    return cacheImpls.get(name);
  }

  /**
   * 获取缓存名称集合
   *
   * @return 缓存名称集合
   */
  public static Collection<String> cacheNames() {
    return cacheImpls.keySet();
  }

  /**
   * 清除缓存
   */
  public static void clear() {
    for (Cache cache : cacheImpls.values()) {
      cache.clear();
    }
    cacheImpls.clear();
  }

  private static Cache createCache(String name) {
    CoreConf config = ConfigFactory.load(CoreConf.class);
    if (config.getCacheImplClass() != null && config.getCacheImplClass().length() > 0) {
      try {
        Class<?> clazz = ClassUtils.forName(config.getCacheImplClass());
        return (Cache) clazz.getConstructor(String.class).newInstance(name);
      } catch (Exception e) {
        throw Exceptions.wrap(e);
      }
    } else {
      return new LocalCacheImpl(name);
    }
  }
}
