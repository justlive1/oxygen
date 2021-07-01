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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.SecurityChecker;
import vip.justlive.oxygen.core.util.concurrent.SecurityThreadPoolExecutor.PoolQueue;

/**
 * @author wubo
 */
@Slf4j
public class SecurityThreadPoolExecutorTest {

  @Test
  public void test() {

    Bootstrap.registerUncaughtExceptionHandler();

    PoolQueue queue = new PoolQueue();
    SecurityThreadPoolExecutor pool = new SecurityThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS,
        queue);
    queue.setPool(pool);

    for (int i = 0; i < 5; i++) {
      pool.execute(System.out::println);
    }

    ThreadUtils.sleep(1200);
    Assert.assertEquals(1, pool.getPoolSize());

    SecurityChecker checker = new SecurityChecker();
    pool.setSecurityChecker(checker);
    Thread thread = new Thread();
    OwnerThreadChecker ock = new OwnerThreadChecker(thread);
    checker.addChecker(ock);

    try {
      pool.shutdown();
      Assert.fail();
    } catch (IllegalStateException e) {
      Assert.assertEquals("current thread is not the owner", e.getMessage());
    }

    checker.delChecker(ock);

    AtomicInteger f = new AtomicInteger();

    TimingFuture<?> future = pool.submit(() -> {
      log.info("start.");
      ThreadUtils.sleep(3000);
      log.info("completed.");
    });

    Assert.assertTrue(future.afterRunning(f::incrementAndGet, 2, TimeUnit.SECONDS));
    Assert.assertTrue(future.afterRunning(() -> {
      log.info("timing sleep 3100ms");
      try {
        Thread.sleep(3100);
      } catch (InterruptedException e) {
        throw Exceptions.wrap(e);
      }
      f.incrementAndGet();
    }, 2, TimeUnit.SECONDS));

    ThreadUtils.sleep(3500);

    Assert.assertFalse(future.afterRunning(f::incrementAndGet, 2, TimeUnit.SECONDS));

    Assert.assertEquals(1, f.get());

    pool.shutdown();
  }
}