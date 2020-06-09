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
 * 可对多个资源key加锁
 *
 * @author wubo
 */
public interface KeyLock {

  /**
   * 尝试加锁
   *
   * @param key 资源key
   * @return true为加锁成功
   */
  boolean tryLock(String key);

  /**
   * 尝试加锁
   *
   * @param key 资源key
   * @param expire 失效时间，单位毫秒
   * @return true为加锁成功
   */
  boolean tryLock(String key, long expire);

  /**
   * 尝试加锁
   *
   * @param key 资源key
   * @param timeout 超时时间
   * @param unit 时间单位
   * @return true为加锁成功
   */
  boolean tryLock(String key, long timeout, TimeUnit unit);

  /**
   * 尝试加锁
   *
   * @param key 资源key
   * @param expire 失效时间
   * @param timeout 超时时间
   * @param unit 时间单位
   * @return true为加锁成功
   */
  boolean tryLock(String key, long expire, long timeout, TimeUnit unit);

  /**
   * 阻塞加锁直到成功
   *
   * @param key 资源key
   */
  void lock(String key);

  /**
   * 阻塞加锁直到成功
   *
   * @param key 资源key
   * @param expire 失效时间
   */
  void lock(String key, long expire);

  /**
   * 释放锁
   *
   * @param key 资源key
   */
  void unlock(String key);
}
