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

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

/**
 * 常驻线程池
 *
 * @author wubo
 */
public class ResidentPool {

  @Getter
  private final int maxSize;
  private final ThreadPoolExecutor pool;

  public ResidentPool(int maxSize) {
    this.maxSize = maxSize;
    this.pool = new ThreadPoolExecutor(1, maxSize, 1, TimeUnit.MINUTES, new SynchronousQueue<>(),
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("resident-pool-%d").build(),
        this::rejectedExecution);
  }

  /**
   * 执行常驻任务
   *
   * @param runnable 任务
   */
  public void add(Runnable runnable) {
    if (pool.getActiveCount() >= maxSize) {
      throw new IllegalStateException("The number of threads has reached the maximum " + maxSize);
    }
    pool.execute(runnable);
  }

  /**
   * 获取当前任务数
   *
   * @return count
   */
  public int count() {
    return pool.getActiveCount();
  }

  /**
   * 关闭线程池
   */
  public void shutdown() {
    pool.shutdownNow();
  }

  private void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    throw new IllegalStateException("The number of threads has reached the maximum " + maxSize);
  }
}
