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
package vip.justlive.oxygen.core.util.base;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * 有界对象池
 *
 * @param <T> 泛型
 * @author wubo
 */
public class BoundedObjectPool<T> implements ObjectPool<T> {

  private final int size;
  private final Supplier<T> supplier;
  private final List<PoolEntry<T>> pool = new ArrayList<>();
  private final IdentityHashMap<T, PoolEntry<T>> map = new IdentityHashMap<>();
  private final Semaphore semaphore;

  public BoundedObjectPool(int size, Supplier<T> supplier) {
    this.size = size;
    this.supplier = supplier;
    this.semaphore = new Semaphore(size);
  }

  @Override
  public T borrow() {
    try {
      return borrow(0, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    }
  }

  @Override
  public T borrow(long timeout, TimeUnit unit) throws InterruptedException {
    if (timeout <= 0) {
      semaphore.acquire();
    } else if (!semaphore.tryAcquire(timeout, unit)) {
      return null;
    }

    if (pool.size() < size) {
      grow();
    }

    for (PoolEntry<T> entry : pool) {
      if (entry.tryUse()) {
        return entry.object;
      }
    }
    return null;
  }

  @Override
  public void release(T obj) {
    PoolEntry<T> entry = map.get(obj);
    if (entry == null) {
      return;
    }
    entry.inUse.set(false);
    semaphore.release();
  }

  private synchronized void grow() {
    if (pool.size() >= size) {
      return;
    }
    PoolEntry<T> entry = new PoolEntry<>(supplier.get());
    map.put(entry.object, entry);
    pool.add(entry);
  }


  @RequiredArgsConstructor
  static class PoolEntry<T> {

    private final AtomicBoolean inUse = new AtomicBoolean(false);
    private final T object;

    boolean tryUse() {
      return inUse.compareAndSet(false, true);
    }
  }
}
