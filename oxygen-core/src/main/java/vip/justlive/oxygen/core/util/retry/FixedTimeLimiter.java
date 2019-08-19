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
package vip.justlive.oxygen.core.util.retry;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import vip.justlive.oxygen.core.util.MoreObjects;

/**
 * 固定时间 time limiter
 *
 * @param <V> 泛型
 * @author wubo
 */
public class FixedTimeLimiter<V> implements TimeLimiter<V> {

  private final long timeout;
  private final TimeUnit timeUnit;
  private final ExecutorService executorService;

  public FixedTimeLimiter(long timeout, TimeUnit timeUnit, ExecutorService executorService) {
    if (timeout <= 0) {
      throw new IllegalArgumentException(String.format("timeout must be positive:%s", timeout));
    }
    this.timeout = timeout;
    this.timeUnit = MoreObjects.notNull(timeUnit);
    this.executorService = MoreObjects.notNull(executorService);
  }

  @Override
  public V call(Callable<V> callable) throws Exception {
    Future<V> future = executorService.submit(callable);
    try {
      return future.get(timeout, timeUnit);
    } catch (InterruptedException | TimeoutException e) {
      future.cancel(true);
      throw e;
    }
  }
}
