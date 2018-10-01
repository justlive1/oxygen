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
package vip.justlive.oxygen.core.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * cache 调用入口
 *
 * @author wubo
 */
public final class JCache {

  private static final boolean EHCACHE_ENABLED = ClassUtils.isPresent("net.sf.ehcache.Cache");

  JCache() {
  }

  static Map<String, Cache> cacheImpls = new ConcurrentHashMap<>(4, 1);

  /**
   * 初始化
   *
   * @param name 缓存名称
   * @param cacheImpl 缓存实现
   */
  public static void init(String name, Cache cacheImpl) {
    cacheImpls.put(name, cacheImpl);
  }

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
      cacheImpls.putIfAbsent(name, createCache());
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

  static Cache createCache() {
    String cacheImplClass = ConfigFactory.getProperty(Constants.CACHE_IMPL_CLASS);
    if (cacheImplClass != null) {
      try {
        Class<?> clazz = ClassUtils.forName(cacheImplClass);
        return (Cache) clazz.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw Exceptions.wrap(e);
      }
    } else {
      if (EHCACHE_ENABLED) {
        return new EhCacheImpl();
      }
      return new LocalCacheImpl();
    }
  }
}
