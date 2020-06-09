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

package vip.justlive.oxygen.core.util.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * 限流
 *
 * @author wubo
 */
public interface RateLimiter {

  /**
   * 设置限流速率
   *
   * @param key 资源key
   * @param rate 单位时间内最大资源数
   * @param interval 单位时间
   */
  void setRate(String key, long rate, long interval);

  /**
   * 尝试获取一个资源
   *
   * @param key 资源key
   * @return true为获取到
   */
  default boolean tryAcquire(String key) {
    return tryAcquire(key, 1);
  }

  /**
   * 尝试获取多个资源
   *
   * @param key 资源key
   * @param permits 资源
   * @return true为能获取到
   */
  boolean tryAcquire(String key, long permits);

  /**
   * 尝试获取资源并设置超时时间
   *
   * @param key 资源key
   * @param timeout 超时时间
   * @param unit 时间单位
   * @return true为能获取到
   */
  default boolean tryAcquire(String key, long timeout, TimeUnit unit) {
    return tryAcquire(key, 1, timeout, unit);
  }

  /**
   * 尝试获取多个资源并设置超时时间
   *
   * @param key 资源key
   * @param permits 资源
   * @param timeout 超时时间
   * @param unit 时间单位
   * @return true为能获取到
   */
  boolean tryAcquire(String key, long permits, long timeout, TimeUnit unit);

  /**
   * 阻塞直到获取资源
   *
   * @param key 资源key
   */
  default void acquire(String key) {
    acquire(key, 1);
  }

  /**
   * 阻塞直到获取指定资源
   *
   * @param key 资源key
   * @param permits 资源
   */
  void acquire(String key, long permits);
}
