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

package vip.justlive.oxygen.core.util.concurrent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import vip.justlive.oxygen.core.util.base.SecurityChecker;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * 增加安全校验的线程池
 * <p>
 * 使用PoolQueue可先创建线程达到maximumPoolSize，而不是等队列满了才创建线程
 *
 * @author wubo
 */
@Slf4j
@Accessors(chain = true)
public class SecurityThreadPoolExecutor extends ThreadPoolExecutor {

  static final String UNNAMED_PREFIX = "Unnamed-";
  private static final AtomicInteger COUNT = new AtomicInteger();
  private final AtomicInteger submittedCount = new AtomicInteger(0);
  @Getter
  private final String name;
  @Setter
  @Getter
  private SecurityChecker securityChecker;

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    this(UNNAMED_PREFIX + COUNT.getAndIncrement(), corePoolSize, maximumPoolSize, keepAliveTime,
        unit, workQueue);
  }

  public SecurityThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize,
      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    this.name = name;
    ThreadPoolMonitor.add(this);
  }

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
    this(UNNAMED_PREFIX + COUNT.getAndIncrement(), corePoolSize, maximumPoolSize, keepAliveTime,
        unit, workQueue, threadFactory);
  }

  public SecurityThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize,
      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    this.name = name;
    ThreadPoolMonitor.add(this);
  }

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
    this(UNNAMED_PREFIX + COUNT.getAndIncrement(), corePoolSize, maximumPoolSize, keepAliveTime,
        unit, workQueue, handler);
  }

  public SecurityThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize,
      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
      RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    this.name = name;
    ThreadPoolMonitor.add(this);
  }

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
      RejectedExecutionHandler handler) {
    this(UNNAMED_PREFIX + COUNT.getAndIncrement(), corePoolSize, maximumPoolSize, keepAliveTime,
        unit, workQueue, threadFactory, handler);
  }

  public SecurityThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize,
      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    this.name = name;
    ThreadPoolMonitor.add(this);
  }

  @Override
  public void execute(Runnable command) {
    submittedCount.incrementAndGet();
    try {
      Runnable runnable = command;
      if (!(runnable instanceof DecoratedTask)) {
        runnable = new DecoratedTask(MDC.getCopyOfContextMap(), runnable);
      }
      super.execute(runnable);
    } catch (RejectedExecutionException e) {
      if (getQueue() instanceof PoolQueue && ((PoolQueue) getQueue()).force(command)) {
        return;
      }
      submittedCount.decrementAndGet();
      throw e;
    }
  }

  @Override
  public void shutdown() {
    if (securityChecker != null) {
      securityChecker.checkPermission();
    }
    ThreadPoolMonitor.remove(this);
    super.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    if (securityChecker != null) {
      securityChecker.checkPermission();
    }
    ThreadPoolMonitor.remove(this);
    return super.shutdownNow();
  }

  @Override
  protected <T> TimingFutureTask<T> newTaskFor(Callable<T> callable) {
    return new TimingFutureTask<>(callable);
  }

  @Override
  protected <T> TimingFutureTask<T> newTaskFor(Runnable runnable, T value) {
    return new TimingFutureTask<>(runnable, value);
  }

  @Override
  public TimingFuture<?> submit(Runnable task) {
    return (TimingFuture<?>) super.submit(task);
  }

  @Override
  public <T> TimingFuture<T> submit(Callable<T> task) {
    return (TimingFuture<T>) super.submit(task);
  }

  @Override
  public <T> TimingFuture<T> submit(Runnable task, T result) {
    return (TimingFuture<T>) super.submit(task, result);
  }

  public int getSubmittedCount() {
    return submittedCount.get();
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    int value = submittedCount.getAndDecrement();
    if (t != null) {
      log.error("pool execute task error submitted: {}, executor: {}", value, this, t);
    }
  }

  @Override
  public String toString() {
    return super.toString() + "-[pool name = " + name + ", maximum pool size = "
        + getMaximumPoolSize() + "]";
  }

  @Setter
  @NoArgsConstructor
  public static class PoolQueue extends LinkedBlockingQueue<Runnable> {

    private static final long serialVersionUID = 5267382526416848275L;

    private transient SecurityThreadPoolExecutor pool;

    PoolQueue(int capacity) {
      super(capacity);
    }

    public boolean force(Runnable runnable) {
      return super.offer(runnable);
    }

    @Override
    public boolean offer(Runnable runnable) {
      if (pool == null) {
        return super.offer(runnable);
      }
      int poolSize = pool.getPoolSize();
      // 当前线程小于最大线程数且已提交的任务大于当前worker数量，触发addWorker方法
      if (poolSize < pool.getMaximumPoolSize() && pool.getSubmittedCount() > poolSize) {
        return false;
      }
      return super.offer(runnable);
    }
  }


  @RequiredArgsConstructor
  public static class DecoratedTask implements Runnable {

    private final Map<String, String> contextMap;
    private final Runnable command;

    @Override
    public void run() {
      try {
        if (contextMap != null) {
          contextMap.forEach(MDC::put);
        }
        MDC.put(Strings.SPAN_ID, ThreadUtils.nextSpanId());
        command.run();
      } finally {
        if (contextMap != null) {
          contextMap.keySet().forEach(MDC::remove);
        }
        MDC.remove(Strings.SPAN_ID);
      }
    }
  }

}
