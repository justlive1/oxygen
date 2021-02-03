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

import vip.justlive.oxygen.core.CoreConfigKeys;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * 根据class构造缓存
 *
 * @author wubo
 */
public class ClassCacheFactory implements CacheFactory {

  @Override
  public Cache create(String name) {
    String cacheClass = CoreConfigKeys.CACHE_CLASS.getValue();
    if (Strings.hasText(cacheClass)) {
      try {
        return (Cache) ClassUtils.forName(cacheClass).getConstructor(String.class)
            .newInstance(name);
      } catch (Exception e) {
        throw Exceptions.wrap(e);
      }
    } else {
      return new LocalCacheImpl(name);
    }
  }
}
