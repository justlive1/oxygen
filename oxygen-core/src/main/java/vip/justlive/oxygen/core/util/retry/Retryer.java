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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import vip.justlive.oxygen.core.exception.WrappedException;

/**
 * 重试器
 *
 * @param <T> 泛型
 * @author wubo
 */
public class Retryer<T> {

  TimeLimiter<T> timeLimiter;
  /**
   * 重试判断
   */
  Predicate<Attempt<T>> retryPredicate;
  /**
   * 终止判断
   */
  Predicate<Attempt<T>> stopPredicate;
  /**
   * 阻塞策略
   */
  Consumer<Attempt<T>> blockConsumer;
  /**
   * 重试监听
   */
  List<Consumer<Attempt<T>>> retryListeners;
  /**
   * 失败监听
   */
  List<Consumer<Attempt<T>>> failListeners;
  /**
   * 成功监听
   */
  List<Consumer<Attempt<T>>> successListeners;

  /**
   * 执行
   *
   * @param callable Callable
   * @return result
   */
  public T call(Callable<T> callable) {
    long startTime = System.currentTimeMillis();
    long attemptNumbers = 0;
    while (true) {
      attemptNumbers++;
      final Attempt<T> attempt = attempt(callable, attemptNumbers, startTime);
      // on retry
      retryListeners.forEach(listener -> listener.accept(attempt));
      // should retry
      if (!retryPredicate.test(attempt)) {
        if (attempt.hasException()) {
          failListeners.forEach(listener -> listener.accept(attempt));
          return null;
        } else {
          successListeners.forEach(listener -> listener.accept(attempt));
          return attempt.getResult();
        }
      }
      // should stop
      if (stopPredicate.test(attempt)) {
        failListeners.forEach(listener -> listener.accept(attempt));
        return null;
      }
      // block
      blockConsumer.accept(attempt);
    }
  }

  Attempt<T> attempt(Callable<T> callable, long attemptNumbers, long startTime) {
    Attempt<T> attempt;
    try {
      T value = timeLimiter.call(callable);
      attempt = new Attempt<>(attemptNumbers, value, System.currentTimeMillis() - startTime);
    } catch (WrappedException e) {
      attempt = new Attempt<>(attemptNumbers, (Exception) e.getException(),
          System.currentTimeMillis() - startTime);
    }
    return attempt;
  }

}
