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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 异步重试器
 *
 * @param <T> 泛型
 * @author wubo
 */
public class AsyncRetryer<T> extends Retryer<T> {

  private final ScheduledExecutorService scheduledExecutorService;
  private final long waitTime;

  AsyncRetryer(TimeLimiter<T> timeLimiter, Predicate<Attempt<T>> retryPredicate,
      Predicate<Attempt<T>> stopPredicate, Consumer<Attempt<T>> blockConsumer,
      List<Consumer<Attempt<T>>> retryListeners, List<Consumer<Attempt<T>>> failListeners,
      List<Consumer<Attempt<T>>> successListeners,
      ScheduledExecutorService scheduledExecutorService, long waitTime) {
    super(timeLimiter, retryPredicate, stopPredicate, blockConsumer, retryListeners, failListeners,
        successListeners);
    this.scheduledExecutorService = scheduledExecutorService;
    this.waitTime = waitTime;
  }

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
      Attempt<T> attempt;
      try {
        T value = timeLimiter.call(callable);
        attempt = new Attempt<>(attemptNumbers, value, System.currentTimeMillis() - startTime);
      } catch (Exception e) {
        attempt = new Attempt<>(attemptNumbers, e, System.currentTimeMillis() - startTime);
      }
      // on retry
      final Attempt<T> tAttempt = attempt;
      retryListeners.forEach(listener -> listener.accept(tAttempt));
      // should retry
      if (!retryPredicate.test(tAttempt)) {
        if (tAttempt.hasException()) {
          failListeners.forEach(listener -> listener.accept(tAttempt));
          future.completeExceptionally(tAttempt.getException());
        } else {
          successListeners.forEach(listener -> listener.accept(tAttempt));
          future.complete(tAttempt.getResult());
        }
      }
      if (future.isDone()) {
        return;
      }
      // should stop
      if (stopPredicate.test(tAttempt)) {
        failListeners.forEach(listener -> listener.accept(tAttempt));
        future.completeExceptionally(tAttempt.getException());
      }
      // block
      scheduledExecutorService
          .schedule(createRunner(callable, startTime, attemptNumbers + 1, future), waitTime,
              TimeUnit.MILLISECONDS);
    };
  }

}
