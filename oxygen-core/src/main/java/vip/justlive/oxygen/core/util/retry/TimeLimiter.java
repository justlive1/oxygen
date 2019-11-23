/*
 * Copyright (C) 2019 the original author or authors.
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
package vip.justlive.oxygen.core.util.retry;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * time limit
 *
 * @param <V> 泛型
 * @author wubo
 */
public interface TimeLimiter<V> {

  /**
   * 无限制
   *
   * @param <V> 泛型
   * @return NoTimeLimiter
   */
  static <V> TimeLimiter<V> noTimeLimit() {
    return new NoTimeLimiter<>();
  }

  /**
   * 固定时间限制
   *
   * @param timeout 超时时间
   * @param timeUnit 单位
   * @param executorService 线程池
   * @param <V> 泛型
   * @return FixedTimeLimiter
   */
  static <V> TimeLimiter<V> fixedTimeLimit(long timeout, TimeUnit timeUnit,
      ExecutorService executorService) {
    return new FixedTimeLimiter<>(timeout, timeUnit, executorService);
  }

  /**
   * 执行
   *
   * @param callable Callable
   * @return result
   */
  V call(Callable<V> callable);
}
