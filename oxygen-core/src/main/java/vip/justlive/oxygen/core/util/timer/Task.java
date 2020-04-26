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

package vip.justlive.oxygen.core.util.timer;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 任务
 *
 * @author wubo
 */
public class Task<T> extends FutureTask<T> implements ScheduledFuture<T> {

  long deadline;

  Task<?> next;
  Task<?> prev;
  Slot slot;

  Task(long deadline, Callable<T> callable) {
    super(callable);
    this.deadline = deadline;
  }

  Task(long deadline, Runnable runnable) {
    super(runnable, null);
    this.deadline = deadline;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (slot != null) {
      slot.remove(this);
    }
    return super.cancel(mayInterruptIfRunning);
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(Delayed o) {
    return Long.compare(deadline, ((Task<?>) o).deadline);
  }
}
