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

package vip.justlive.oxygen.core.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * ExecutorCompletionService扩展
 *
 * @param <V> 泛型
 * @author wubo
 */
@Slf4j
public class ExecutorPool<V> extends ExecutorCompletionService<V> {

  private AtomicInteger count = new AtomicInteger(0);

  /**
   * 使用cached pool构造
   */
  public ExecutorPool() {
    this(ThreadUtils.cachedPool());
  }


  /**
   * 构造pool
   *
   * @param executor 线程池
   */
  public ExecutorPool(Executor executor) {
    super(executor);
  }

  /**
   * 构造pool
   *
   * @param executor 线程池
   * @param completionQueue 队列
   */
  public ExecutorPool(Executor executor, BlockingQueue<Future<V>> completionQueue) {
    super(executor, completionQueue);
  }

  @Override
  public Future<V> submit(Callable<V> task) {
    Future<V> future = super.submit(task);
    count.incrementAndGet();
    return future;
  }

  @Override
  public Future<V> submit(Runnable task, V result) {
    Future<V> future = super.submit(task, result);
    count.incrementAndGet();
    return future;
  }

  @Override
  public Future<V> take() throws InterruptedException {
    Future<V> future = super.take();
    count.decrementAndGet();
    return future;
  }

  @Override
  public Future<V> poll() {
    Future<V> future = super.poll();
    if (future != null) {
      count.decrementAndGet();
    }
    return future;
  }

  @Override
  public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
    Future<V> future = super.poll(timeout, unit);
    if (future != null) {
      count.decrementAndGet();
    }
    return future;
  }

  /**
   * 执行runnable
   *
   * @param task runnable
   * @return future
   */
  public Future<V> submit(Runnable task) {
    return submit(task, null);
  }

  /**
   * 等待处理结果，出现异常则中断等待
   *
   * @return result
   * @throws InterruptedException 中断异常
   * @throws ExecutionException 执行异常
   */
  public List<V> waitFor() throws InterruptedException, ExecutionException {
    List<V> list = new LinkedList<>();
    while (count.get() > 0) {
      list.add(take().get());
    }
    return list;
  }

  /**
   * 等待处理结果，出现异常继续等待其他执行结果
   *
   * @return result
   */
  public List<V> waitForAll() {
    List<V> list = new LinkedList<>();
    while (count.get() > 0) {
      try {
        list.add(take().get());
      } catch (Exception e) {
        log.error("wait for result error", e);
      }
    }
    return list;
  }
}
