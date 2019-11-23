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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * cache store
 *
 * @author wubo
 */
@UtilityClass
class CacheStore {

  static final Map<String, Cache> CACHES = new ConcurrentHashMap<>(4, 1);

  /**
   * 创建cache
   *
   * @param name 缓存名称
   * @return cache
   */
  static Cache createCache(String name) {
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
