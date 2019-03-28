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
package vip.justlive.oxygen.cache.store;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Ehcache实现
 *
 * @author wubo
 */
public class EhCacheImpl implements Cache {

  private static final AtomicLong ATOMIC = new AtomicLong();
  private static final String NAME_TEMPLATE = "EhCache-%s";

  private net.sf.ehcache.Cache cache;

  public EhCacheImpl() {
    this(String.format(NAME_TEMPLATE, ATOMIC.getAndIncrement()));
  }

  public EhCacheImpl(String name) {
    CacheManager cacheManager = CacheManager.create();
    cacheManager.addCache(name);
    this.cache = cacheManager.getCache(name);
  }

  @Override
  public Object get(String key) {
    Element e = cache.get(key);
    return (e == null) ? null : e.getObjectValue();
  }

  @Override
  public <T> T get(String key, Class<T> clazz) {
    return clazz.cast(get(key));
  }

  @Override
  public Map<String, Object> get(String... keys) {
    Map<String, Object> result = new HashMap<>(keys.length);
    for (String key : keys) {
      result.put(key, get(key));
    }
    return result;
  }

  @Override
  public Object putIfAbsent(String key, Object value) {
    Element element = new Element(key, value);
    element = cache.putIfAbsent(element);
    return element == null ? null : element.getObjectValue();
  }

  @Override
  public Object putIfAbsent(String key, Object value, long duration, TimeUnit unit) {
    Element element = new Element(key, value);
    element.setTimeToLive((int) unit.toSeconds(duration));
    element = cache.putIfAbsent(element);
    return element == null ? null : element.getObjectValue();
  }

  @Override
  public synchronized Object set(String key, Object value) {
    Element element = new Element(key, value);
    Object val = get(key);
    cache.put(element);
    return val;
  }

  @Override
  public synchronized Object set(String key, Object value, long duration, TimeUnit unit) {
    Element element = new Element(key, value);
    element.setTimeToLive((int) unit.toSeconds(duration));
    Object val = get(key);
    cache.put(element);
    return val;
  }

  @Override
  public Object replace(String key, Object value) {
    Element element = new Element(key, value);
    element = cache.replace(element);
    return element == null ? null : element.getObjectValue();
  }

  @Override
  public Object replace(String key, Object value, long duration, TimeUnit unit) {
    Element element = new Element(key, value);
    element.setTimeToLive((int) unit.toSeconds(duration));
    element = cache.replace(element);
    return element == null ? null : element.getObjectValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<String> keys() {
    return cache.getKeys();
  }

  @Override
  public void remove(String... keys) {
    cache.removeAll(Arrays.asList(keys));
  }

  @Override
  public void clear() {
    cache.removeAll();
  }

}
