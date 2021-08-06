/*
 * Copyright (C) 2021 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 特定时机任务实现
 *
 * @param <T> 泛型
 * @author wubo
 */
public class TimingFutureTask<T> extends FutureTask<T> implements TimingFuture<T> {

  private final List<Future<?>> timingFutures = new ArrayList<>();

  public TimingFutureTask(Callable<T> callable) {
    super(callable);
  }

  public TimingFutureTask(Runnable runnable, T result) {
    super(runnable, result);
  }

  @Override
  protected void done() {
    for (Future<?> future : timingFutures) {
      future.cancel(true);
    }
    timingFutures.clear();
  }

  @Override
  public boolean afterRunning(Runnable runnable, long time, TimeUnit unit) {
    if (runnable == null || isDone() || isCancelled()) {
      return false;
    }

    ScheduledFuture<Void> future = ThreadUtils.globalTimer().schedule(runnable, time, unit);
    timingFutures.add(future);
    return true;
  }
}
