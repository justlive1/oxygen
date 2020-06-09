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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import vip.justlive.oxygen.core.util.base.ExpiringMap;

/**
 * 本地缓存实现
 *
 * @author wubo
 */
public class LocalCacheImpl implements Cache {

  private final String name;
  private final ExpiringMap<String, Object> store;

  public LocalCacheImpl(String name) {
    this.name = name;
    this.store = ExpiringMap.<String, Object>builder().name(name).build();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Object get(String key) {
    return store.get(key);
  }

  @Override
  public <T> T get(String key, Class<T> clazz) {
    return clazz.cast(get(key));
  }

  @Override
  public Map<String, Object> get(String... keys) {
    Map<String, Object> map = new HashMap<>(8);
    for (String key : keys) {
      map.put(key, get(key));
    }
    return map;
  }

  @Override
  public Object putIfAbsent(String key, Object value) {
    return store.putIfAbsent(key, value);
  }

  @Override
  public Object putIfAbsent(String key, Object value, long duration, TimeUnit unit) {
    return store.putIfAbsent(key, value, duration, unit);
  }

  @Override
  public Object set(String key, Object value) {
    return store.put(key, value);
  }

  @Override
  public Object set(String key, Object value, long duration, TimeUnit unit) {
    return store.put(key, value, duration, unit);
  }

  @Override
  public Object replace(String key, Object value) {
    return store.replace(key, value);
  }

  @Override
  public Object replace(String key, Object value, long duration, TimeUnit unit) {
    return store.replace(key, value, duration, unit);
  }

  @Override
  public Collection<String> keys() {
    return store.keySet();
  }

  @Override
  public void remove(String... keys) {
    for (String key : keys) {
      store.remove(key);
    }
  }

  @Override
  public synchronized long incr(String key, int by) {
    Long value = get(key, Long.class);
    if (value == null) {
      value = (long) by;
      set(key, value);
      return by;
    } else {
      value += by;
      replace(key, value);
      return value;
    }
  }

  @Override
  public void clear() {
    store.clear();
  }

}
