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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存接口
 *
 * @author wubo
 */
public interface Cache {

  /**
   * 获取缓存对象
   *
   * @param key cache key
   * @return the cached object or null
   */
  Object get(String key);

  /**
   * 获取缓存对象
   *
   * @param key cache key
   * @param clazz the type of cache
   * @param <T> type
   * @return the cached object or null
   */
  <T> T get(String key, Class<T> clazz);

  /**
   * 批量获取缓存对象
   *
   * @param keys cache keys
   * @return return key-value objects
   */
  Map<String, Object> get(String... keys);

  /**
   * 判断缓存是否存在
   *
   * @param key cache key
   * @return true if key exists
   */
  default boolean exists(String key) {
    return get(key) != null;
  }

  /**
   * 向缓存中存放对象，当前缓存中不存在key值时执行
   *
   * @param key cache key
   * @param value cache value
   * @return exist cache value
   */
  Object putIfAbsent(String key, Object value);

  /**
   * 向缓存中存放对象，当前缓存中不存在key值时执行
   *
   * @param key cache key
   * @param value cache value
   * @param duration duration
   * @param unit timeunit
   * @return exist cache value
   */
  Object putIfAbsent(String key, Object value, long duration, TimeUnit unit);

  /**
   * 向缓存中存放对象
   *
   * @param key cache key
   * @param value cache value
   * @return exist cache value
   */
  Object set(String key, Object value);

  /**
   * 向缓存中存放对象
   *
   * @param key cache key
   * @param value cache value
   * @param duration duration
   * @param unit timeunit
   * @return exist cache value
   */
  Object set(String key, Object value, long duration, TimeUnit unit);


  /**
   * 替换缓存中key对应的value
   *
   * @param key cache key
   * @param value cache value
   * @return exist cache value
   */
  Object replace(String key, Object value);

  /**
   * 替换缓存中key对应的value
   *
   * @param key cache key
   * @param value cache value
   * @param duration duration
   * @param unit timeunit
   * @return exist cache value
   */
  Object replace(String key, Object value, long duration, TimeUnit unit);

  /**
   * Return all keys
   *
   * @return 返回键的集合
   */
  Collection<String> keys();

  /**
   * Remove items from the cache
   *
   * @param keys Cache key
   */
  void remove(String... keys);

  /**
   * Clear the cache
   */
  void clear();
}
