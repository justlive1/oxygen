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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * 简单对象池
 *
 * @param <T> 泛型
 * @author wubo
 */
@RequiredArgsConstructor
public class SimpleObjectPool<T> implements ObjectPool<T> {

  private final Supplier<T> supplier;
  private final Queue<T> queue = new ConcurrentLinkedQueue<>();

  @Override
  public T borrow() {
    T obj = queue.poll();
    if (obj != null) {
      return obj;
    }
    return supplier.get();
  }

  @Override
  public T borrow(long timeout, TimeUnit unit) {
    return borrow();
  }

  @Override
  public void release(T obj) {
    if (obj == null || queue.contains(obj)) {
      return;
    }
    queue.offer(obj);
  }

  public void clear() {
    queue.clear();
  }
}
