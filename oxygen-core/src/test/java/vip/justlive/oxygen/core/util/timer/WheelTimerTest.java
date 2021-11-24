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

package vip.justlive.oxygen.core.util.timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * @author wubo
 */
@Slf4j
class WheelTimerTest {


  @Test
  void test() throws ExecutionException, InterruptedException {
    WheelTimer timer = new WheelTimer(1000, 5);
    timer.start();

    AtomicInteger count = new AtomicInteger();
    Callable<Integer> callable = () -> {
      int c = count.incrementAndGet();
      log.info("xxx {}", c);
      return c;
    };

    timer.schedule(callable, 2, TimeUnit.SECONDS);
    timer.schedule(callable, 3, TimeUnit.SECONDS);
    Future<Integer> future = timer.schedule(callable, 7, TimeUnit.SECONDS);

    assertEquals(0, count.get());

    ThreadUtils.sleep(2000);
    assertEquals(1, count.get());

    ThreadUtils.sleep(3000);
    assertEquals(2, count.get());

    ThreadUtils.sleep(3000);
    assertEquals(3, count.get());

    System.out.println(future.get());

    timer.shutdown();

    assertTrue(timer.isShutdown());
  }

  @Test
  void test1() {
    WheelTimer timer = ThreadUtils.globalTimer();

    AtomicInteger count = new AtomicInteger();
    ScheduledFuture<Void> rate = timer
        .scheduleAtFixedRate(() -> call(count), 1, 2, TimeUnit.SECONDS);
    ScheduledFuture<Void> delay = timer
        .scheduleWithFixedDelay(() -> call(count), 1, 2, TimeUnit.SECONDS);

    Callable<Integer> callable = () -> {
      int c = count.incrementAndGet();
      log.info("xxx {}", c);
      return c;
    };

    timer.schedule(callable, 52, TimeUnit.MINUTES);
    timer.schedule(callable, 52, TimeUnit.HOURS);
    timer.schedule(callable, 365 * 400, TimeUnit.DAYS);

    ThreadUtils.sleep(5900);
    assertEquals(5, count.get());

    rate.cancel(true);

    ThreadUtils.sleep(8000);

    assertTrue(rate.isDone());
    assertFalse(delay.isDone());

    assertThrows(IllegalStateException.class, timer::shutdown);

  }

  @Test
  void test2() {
    WheelTimer timer = new WheelTimer(1000, 20, 2);

    AtomicInteger count = new AtomicInteger();
    ScheduledFuture<Void> rate = timer
        .scheduleAtFixedRate(() -> call(count), 1, 2, TimeUnit.SECONDS);
    ScheduledFuture<Void> delay = timer
        .scheduleWithFixedDelay(() -> call(count), 1, 2, TimeUnit.SECONDS);

    Runnable runnable = () -> {
      int c = count.getAndIncrement();
      log.info("xxx {}", c);
    };

    ScheduledFuture<Void> f = timer.schedule(runnable, 20, TimeUnit.SECONDS);
    timer.schedule(runnable, 7, TimeUnit.SECONDS);

    ThreadUtils.sleep(5900);
    assertEquals(5, count.get());

    rate.cancel(true);
    f.cancel(true);

    ThreadUtils.sleep(8000);

    assertTrue(rate.isDone());
    assertFalse(delay.isDone());

    timer.shutdown();

  }

  private void call(AtomicInteger count) {
    log.info("call {}", count.getAndIncrement());
    ThreadUtils.sleep(1000);
  }

}