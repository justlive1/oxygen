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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import vip.justlive.oxygen.core.util.base.SecurityChecker;

/**
 * 增加安全校验的线程池
 *
 * 使用PoolQueue可先创建线程达到maximumPoolSize，而不是等队列满了才创建线程
 *
 * @author wubo
 */
@Accessors(chain = true)
public class SecurityThreadPoolExecutor extends ThreadPoolExecutor {

  private final AtomicInteger submittedCount = new AtomicInteger(0);
  @Setter
  @Getter
  private SecurityChecker securityChecker;

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
  }

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
  }

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
      RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
  }

  @Override
  public void execute(Runnable command) {
    submittedCount.incrementAndGet();
    try {
      super.execute(command);
    } catch (RejectedExecutionException e) {
      if (getQueue() instanceof PoolQueue && getQueue().offer(command)) {
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
    super.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    if (securityChecker != null) {
      securityChecker.checkPermission();
    }
    return super.shutdownNow();
  }

  public int getSubmittedCount() {
    return submittedCount.get();
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    submittedCount.decrementAndGet();
  }

  @NoArgsConstructor
  public static class PoolQueue extends LinkedBlockingQueue<Runnable> {

    private static final long serialVersionUID = 5267382526416848275L;

    @Setter
    private transient SecurityThreadPoolExecutor pool;

    PoolQueue(int capacity) {
      super(capacity);
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

}
