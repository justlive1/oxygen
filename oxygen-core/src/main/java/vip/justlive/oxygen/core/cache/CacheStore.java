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

package vip.justlive.oxygen.core.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.util.base.MoreObjects;

/**
 * cache store
 *
 * @author wubo
 */
@UtilityClass
public class CacheStore {

  final Map<String, Cache> CACHES = new ConcurrentHashMap<>(4, 1);
  private final AtomicReference<CacheFactory> CACHE_FACTORY = new AtomicReference<>(
      new ClassCacheFactory());

  /**
   * 创建cache
   *
   * @param name 缓存名称
   * @return cache
   */
  Cache createCache(String name) {
    return CACHE_FACTORY.get().create(name);
  }

  /**
   * 清除缓存引用，不清除缓存内容
   */
  public void clear() {
    CACHES.clear();
  }

  /**
   * 设置缓存工厂
   *
   * @param cacheFactory 缓存工厂
   */
  public void setCacheFactory(CacheFactory cacheFactory) {
    MoreObjects.notNull(cacheFactory, "cache factory can not be null");
    if (CACHE_FACTORY.get() != cacheFactory) {
      clear();
    }
    CACHE_FACTORY.set(cacheFactory);
  }
}
