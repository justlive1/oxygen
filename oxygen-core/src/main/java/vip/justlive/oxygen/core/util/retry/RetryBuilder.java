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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import vip.justlive.oxygen.core.util.Checks;
import vip.justlive.oxygen.core.util.ThreadUtils;

/**
 * 重试构造器
 *
 * @param <T> 泛型
 * @author wubo
 */
public class RetryBuilder<T> {

  /**
   * 重试判断
   */
  private Predicate<Attempt<T>> retryPredicate = attempt -> false;
  /**
   * 终止判断
   */
  private Predicate<Attempt<T>> stopPredicate = attempt -> false;
  /**
   * 阻塞策略
   */
  private Consumer<Attempt<T>> blockConsumer = attempt -> {
  };
  /**
   * 重试监听
   */
  private List<Consumer<Attempt<T>>> retryListeners = new LinkedList<>();
  /**
   * 失败监听
   */
  private List<Consumer<Attempt<T>>> failListeners = new LinkedList<>();
  /**
   * 成功监听
   */
  private List<Consumer<Attempt<T>>> successListeners = new LinkedList<>();
  /**
   * 等待时间
   */
  private long waitTime = 0;
  /**
   * 超时限制器
   */
  private TimeLimiter<T> timeLimiter;
  private ScheduledExecutorService scheduledExecutorService;

  private RetryBuilder() {
  }

  /**
   * 创建构造器
   *
   * @param <T> 泛型
   * @return builder
   */
  public static <T> RetryBuilder<T> newBuilder() {
    return new RetryBuilder<>();
  }

  /**
   * 发生异常重试
   *
   * @return builder
   */
  public RetryBuilder<T> retryIfException() {
    return retryIf(Attempt::hasException);
  }

  /**
   * 发生指定异常进行重试
   *
   * @param exceptionClass 异常类型
   * @return builder
   */
  public RetryBuilder<T> retryIfException(Class<? extends Exception> exceptionClass) {
    return retryIf(attempt -> attempt.hasException() && exceptionClass
        .isAssignableFrom(attempt.getException().getClass()));
  }

  /**
   * 判断结果是否重试
   *
   * @param resultPredicate 结果判断
   * @return builder
   */
  public RetryBuilder<T> retryIfResult(Predicate<T> resultPredicate) {
    return retryIf(attempt -> !attempt.hasException() && resultPredicate.test(attempt.getResult()));
  }

  /**
   * 判断是否重试
   *
   * @param predicate 判断
   * @return builder
   */
  public RetryBuilder<T> retryIf(Predicate<Attempt<T>> predicate) {
    this.retryPredicate = this.retryPredicate.or(predicate);
    return this;
  }

  /**
   * 设置超时限制
   *
   * @param timeout 超时时间
   * @param timeUnit 时间单位
   * @return builder
   */
  public RetryBuilder<T> withTimeLimit(long timeout, TimeUnit timeUnit) {
    return withTimeLimit(timeout, timeUnit,
        ThreadUtils.newThreadPool(5, 10, 100, 1000, "retry-time-limiter-%d"));
  }

  /**
   * 设置超时限制
   *
   * @param timeout 超时时间
   * @param timeUnit 时间单位
   * @param executorService 线程池
   * @return builder
   */
  public RetryBuilder<T> withTimeLimit(long timeout, TimeUnit timeUnit,
      ExecutorService executorService) {
    this.timeLimiter = TimeLimiter.fixedTimeLimit(timeout, timeUnit, executorService);
    return this;
  }

  /**
   * 设置最大尝试次数
   *
   * @param maxAttempt 最大尝试次数
   * @return builder
   */
  public RetryBuilder<T> withMaxAttempt(long maxAttempt) {
    this.stopPredicate = this.stopPredicate.or(attempt -> attempt.getAttemptNumber() >= maxAttempt);
    return this;
  }

  /**
   * 设置最大延期时间
   *
   * @param maxDelay 最大延期时间
   * @return builder
   */
  public RetryBuilder<T> withMaxDelay(long maxDelay) {
    this.stopPredicate = this.stopPredicate
        .or(attempt -> attempt.getMillsAfterFirstAttempt() >= maxDelay);
    return this;
  }

  /**
   * 永不停止
   *
   * @return builder
   */
  public RetryBuilder<T> withNeverStop() {
    this.stopPredicate = attempt -> false;
    return this;
  }

  /**
   * 设置尝试之间使用sleep等待
   *
   * @param waitTime 等待时间
   * @return builder
   */
  public RetryBuilder<T> withSleepBlock(long waitTime) {
    if (waitTime < 0) {
      throw new IllegalArgumentException(String.format("waitTime [%s] mast be >= 0", waitTime));
    }
    this.waitTime = waitTime;
    this.blockConsumer = attempt -> ThreadUtils.sleep(waitTime);
    return this;
  }

  /**
   * 设置尝试之间使用wait等待
   *
   * @param waitTime 等待时间
   * @return builder
   */
  public RetryBuilder<T> withWaitBlock(long waitTime) {
    if (waitTime < 0) {
      throw new IllegalArgumentException(String.format("waitTime [%s] mast be >= 0", waitTime));
    }
    this.waitTime = waitTime;
    this.blockConsumer = attempt -> waitBlock(waitTime);
    return this;
  }

  /**
   * 设置自定义等待
   *
   * @param consumer 处理器
   * @return builder
   */
  public RetryBuilder<T> withBlock(Consumer<Attempt<T>> consumer) {
    this.blockConsumer = consumer;
    return this;
  }

  /**
   * 重试监听，当重试时触发
   *
   * @param listener 监听
   * @return builder
   */
  public RetryBuilder<T> onRetry(Consumer<Attempt<T>> listener) {
    retryListeners.add(Checks.notNull(listener));
    return this;
  }

  /**
   * 失败监听，最终失败时触发
   *
   * @param listener 监听
   * @return builder
   */
  public RetryBuilder<T> onFinalFail(Consumer<Attempt<T>> listener) {
    failListeners.add(Checks.notNull(listener));
    return this;
  }

  /**
   * 成功监听，成功时触发
   *
   * @param listener 监听
   * @return builder
   */
  public RetryBuilder<T> onSuccess(Consumer<Attempt<T>> listener) {
    successListeners.add(Checks.notNull(listener));
    return this;
  }

  /**
   * 设置异步线程池
   *
   * @param scheduledExecutorService 线程池
   * @return builder
   */
  public RetryBuilder<T> withAsyncExecutor(ScheduledExecutorService scheduledExecutorService) {
    this.scheduledExecutorService = Checks.notNull(scheduledExecutorService);
    return this;
  }

  /**
   * 构造同步重试器
   *
   * @return Retryer
   */
  public Retryer<T> build() {
    if (timeLimiter == null) {
      timeLimiter = TimeLimiter.noTimeLimit();
    }
    return new Retryer<>(timeLimiter, retryPredicate, stopPredicate, blockConsumer, retryListeners,
        failListeners, successListeners);
  }

  /**
   * 构造异步重试器
   *
   * @return AsyncRetryer
   */
  public AsyncRetryer<T> buildAsync() {
    if (timeLimiter == null) {
      timeLimiter = TimeLimiter.noTimeLimit();
    }
    if (scheduledExecutorService == null) {
      scheduledExecutorService = ThreadUtils.newScheduledExecutor(5, "retry-async-%d");
    }
    return new AsyncRetryer<>(timeLimiter, retryPredicate, stopPredicate, blockConsumer,
        retryListeners, failListeners, successListeners, scheduledExecutorService, waitTime);
  }

  private void waitBlock(long millis) {
    synchronized (this) {
      try {
        wait(millis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

}
