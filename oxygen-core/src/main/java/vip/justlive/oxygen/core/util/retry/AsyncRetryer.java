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
package vip.justlive.oxygen.core.util.retry;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 异步重试器
 *
 * @param <T> 泛型
 * @author wubo
 */
public class AsyncRetryer<T> extends Retryer<T> {

  ScheduledExecutorService scheduledExecutorService;
  long waitTime;

  /**
   * 异步执行
   *
   * @param callable Callable
   * @return CompletableFuture
   */
  public CompletableFuture<T> callAsync(Callable<T> callable) {
    CompletableFuture<T> future = new CompletableFuture<>();
    scheduledExecutorService.submit(createRunner(callable, System.currentTimeMillis(), 1, future));
    return future;
  }

  private Runnable createRunner(Callable<T> callable, long startTime, long attemptNumbers,
      CompletableFuture<T> future) {
    return () -> {
      Attempt<T> attempt = attempt(callable, attemptNumbers, startTime);
      // on retry
      retryListeners.forEach(listener -> listener.accept(attempt));
      // should retry
      if (!retryPredicate.test(attempt)) {
        if (attempt.hasException()) {
          failListeners.forEach(listener -> listener.accept(attempt));
          future.completeExceptionally(attempt.getException());
        } else {
          successListeners.forEach(listener -> listener.accept(attempt));
          future.complete(attempt.getResult());
        }
      }
      if (future.isDone()) {
        return;
      }
      // should stop
      if (stopPredicate.test(attempt)) {
        failListeners.forEach(listener -> listener.accept(attempt));
        future.completeExceptionally(attempt.getException());
      }
      // block
      scheduledExecutorService
          .schedule(createRunner(callable, startTime, attemptNumbers + 1, future), waitTime,
              TimeUnit.MILLISECONDS);
    };
  }

}
