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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 重复执行包装
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class RepeatRunnable implements Runnable {

  private static final AtomicInteger COUNT = new AtomicInteger();
  private final CountDownLatch workInitiated = new CountDownLatch(1);
  private final CountDownLatch shutdownInitiated = new CountDownLatch(1);
  private final CountDownLatch shutdownComplete = new CountDownLatch(1);
  @Getter
  private final String name;
  private final Runnable runnable;
  @Getter
  private volatile boolean started;
  @Getter
  private int rounds;

  public RepeatRunnable(Runnable runnable) {
    this("Unnamed-" + COUNT.getAndIncrement(), runnable);
  }

  @Override
  public void run() {
    String pre = Thread.currentThread().getName();
    if (log.isDebugEnabled()) {
      log.debug("thread name '{}' change to '{}'", pre, name);
    }
    Thread.currentThread().setName(name);
    try {
      doRun();
    } finally {
      Thread.currentThread().setName(pre);
      if (log.isDebugEnabled()) {
        log.debug("thread name '{}' return to '{}'", name, pre);
      }
    }
  }

  /**
   * 等待线程执行run方法
   */
  public void awaitRunning() {
    await(workInitiated);
  }

  /**
   * 终止任务
   */
  public void shutdown() {
    synchronized (this) {
      if (shutdownInitiated.getCount() > 0) {
        log.info("[{}] shutting down", name);
        shutdownInitiated.countDown();
      }
    }
    if (isStarted()) {
      await(shutdownComplete);
      if (shutdownComplete.getCount() == 0) {
        log.info("[{}] shutdown completed", name);
      }
    }
  }

  /**
   * 是否关闭
   *
   * @return true为关闭
   */
  public boolean isShutdown() {
    return shutdownInitiated.getCount() == 0 || shutdownComplete.getCount() == 0;
  }

  private void doRun() {
    started = true;
    log.info("[{}] starting", name);
    workInitiated.countDown();
    try {
      while (shutdownInitiated.getCount() > 0) {
        if (Thread.currentThread().isInterrupted()) {
          log.info("[{}] thread is interrupted, stop repeat at {} rounds", name, rounds);
          break;
        }
        runnable.run();
        rounds++;
      }
    } catch (Exception e) {
      log.error("[{}] error due to", name, e);
    } finally {
      shutdownComplete.countDown();
    }
    log.info("[{}] stopped", name);
  }

  private void await(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
