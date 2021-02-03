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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 对象池接口
 *
 * @param <T> 泛型
 * @author wubo
 */
public interface ObjectPool<T> {

  /**
   * 获取对象
   *
   * @return 对象
   */
  T borrow();

  /**
   * 获取对象
   *
   * @param timeout 超时时间
   * @param unit 单位
   * @return 对象
   * @throws InterruptedException 线程中断
   */
  T borrow(long timeout, TimeUnit unit) throws InterruptedException;

  /**
   * 返还对象
   *
   * @param obj 对象
   */
  void release(T obj);

  /**
   * 获取对象并执行逻辑，最终返还对象
   *
   * @param consumer 执行逻辑
   */
  default void run(Consumer<T> consumer) {
    T obj = borrow();
    try {
      consumer.accept(obj);
    } finally {
      release(obj);
    }
  }

}
