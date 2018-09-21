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
import java.util.concurrent.TimeUnit;

/**
 * cache 调用入口
 *
 * @author wubo
 */
public final class JCache {

  JCache() {
  }

  static Cache cacheImpl;

  /**
   * 初始化
   *
   * @param cacheImpl 缓存实现
   */
  static void init(Cache cacheImpl) {
    JCache.cacheImpl = cacheImpl;
  }

  /**
   * 获取缓存对象
   *
   * @param key cache key
   * @return the cached object or null
   */
  public static Object get(String key) {
    return cacheImpl.get(key);
  }

  /**
   * 批量获取缓存对象
   *
   * @param keys cache keys
   * @return return key-value objects
   */
  public static Map<String, Object> get(String... keys) {
    return cacheImpl.get(keys);
  }

  /**
   * 判断缓存是否存在
   *
   * @param key cache key
   * @return true if key exists
   */
  public static boolean exists(String key) {
    return cacheImpl.exists(key);
  }

  /**
   * 向缓存中存放对象，当前缓存中不存在key值时执行
   *
   * @param key cache key
   * @param value cache value
   * @return exist cache value
   */
  public static Object putIfAbsent(String key, Object value) {
    return cacheImpl.putIfAbsent(key, value);
  }

  /**
   * 向缓存中存放对象，当前缓存中不存在key值时执行
   *
   * @param key cache key
   * @param value cache value
   * @param duration duration
   * @param unit timeunit
   * @return exist cache value
   */
  public static Object putIfAbsent(String key, Object value, long duration, TimeUnit unit) {
    return cacheImpl.putIfAbsent(key, value, duration, unit);
  }

  /**
   * 向缓存中存放对象
   *
   * @param key cache key
   * @param value cache value
   * @return exist cache value
   */
  public static Object set(String key, Object value) {
    return cacheImpl.set(key, value);
  }

  /**
   * 向缓存中存放对象
   *
   * @param key cache key
   * @param value cache value
   * @param duration duration
   * @param unit timeunit
   * @return exist cache value
   */
  public static Object set(String key, Object value, long duration, TimeUnit unit) {
    return cacheImpl.set(key, value, duration, unit);
  }

  /**
   * 替换缓存中key对应的value
   *
   * @param key cache key
   * @param value cache value
   * @return exist cache value
   */
  public static Object replace(String key, Object value) {
    return cacheImpl.replace(key, value);
  }

  /**
   * 替换缓存中key对应的value
   *
   * @param key cache key
   * @param value cache value
   * @param duration duration
   * @param unit timeunit
   * @return exist cache value
   */
  public static Object replace(String key, Object value, long duration, TimeUnit unit) {
    return cacheImpl.replace(key, value, duration, unit);
  }

  /**
   * Return all keys
   *
   * @return 返回键的集合
   */
  public static Collection<String> keys() {
    return cacheImpl.keys();
  }

  /**
   * Remove items from the cache
   *
   * @param keys Cache key
   */
  public static void remove(String... keys) {
    cacheImpl.remove(keys);
  }

  /**
   * Clear the cache
   */
  public static void clear() {
    cacheImpl.clear();
  }

}
