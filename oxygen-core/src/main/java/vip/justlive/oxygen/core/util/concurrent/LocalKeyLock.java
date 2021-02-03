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


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 本地 key Lock
 *
 * @author wubo
 */
public class LocalKeyLock implements KeyLock {

  private final Map<String, Lock> locks = new ConcurrentHashMap<>(4);

  @Override
  public boolean tryLock(String key) {
    return getLock(key).tryLock();
  }

  @Override
  public boolean tryLock(String key, long expire) {
    return tryLock(key);
  }

  @Override
  public boolean tryLock(String key, long timeout, TimeUnit unit) {
    try {
      return getLock(key).tryLock(timeout, unit);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  @Override
  public boolean tryLock(String key, long expire, long timeout, TimeUnit unit) {
    return tryLock(key, timeout, unit);
  }

  @Override
  public void lock(String key) {
    getLock(key).lock();
  }

  @Override
  public void lock(String key, long expire) {
    lock(key);
  }

  @Override
  public void unlock(String key) {
    Lock lock = locks.get(key);
    if (lock != null) {
      lock.unlock();
    }
  }

  private Lock getLock(String key) {
    return locks.computeIfAbsent(key, k -> new ReentrantLock());
  }
}