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
package vip.justlive.oxygen.core.job;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * 简单job线程池实现
 *
 * @author wubo
 */
public class SimpleJobThreadPool implements JobThreadPool {

  private final long slowTimeWindow;
  private final long slowThreshold;
  private final int slowHitLimit;
  private final ThreadPoolExecutor fastPool;
  private final ThreadPoolExecutor slowPool;

  private final Map<String, AtomicInteger> slowHits = new ConcurrentHashMap<>(4);

  private long currentWindow;

  public SimpleJobThreadPool(JobConf conf) {
    slowTimeWindow = conf.getSlowTimeWindow();
    slowThreshold = conf.getSlowThresholdTime();
    slowHitLimit = conf.getSlowHitLimit();
    fastPool = ThreadUtils
        .newThreadPool(1, conf.getThreadCorePoolSize(), 60, conf.getThreadQueueCapacity(),
            "Fast-" + conf.getThreadNameFormat());
    slowPool = ThreadUtils
        .newThreadPool(1, conf.getThreadCorePoolSize(), 60, conf.getThreadQueueCapacity(),
            "Slow-" + conf.getThreadNameFormat());
    currentWindow = System.currentTimeMillis();
  }

  @Override
  public void execute(String jobKey, Runnable runnable) {
    ThreadPoolExecutor pool = fastPool;

    AtomicInteger count = slowHits.get(jobKey);
    if (count != null && count.get() > slowHitLimit) {
      pool = slowPool;
    }

    pool.execute(new Task(jobKey, runnable));
  }

  @RequiredArgsConstructor
  class Task implements Runnable {

    final String key;
    final Runnable runnable;

    @Override
    public void run() {
      long start = System.currentTimeMillis();
      try {
        runnable.run();
      } finally {
        long end = System.currentTimeMillis();

        synchronized (slowHits) {
          if (end - currentWindow > slowTimeWindow) {
            currentWindow = end;
            slowHits.clear();
          }
        }

        if (end - start > slowThreshold) {
          AtomicInteger count = slowHits.computeIfAbsent(key, k -> new AtomicInteger());
          count.incrementAndGet();
        }
      }
    }
  }
}
