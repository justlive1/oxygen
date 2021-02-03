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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * @author wubo
 */
public class RetryerTest {

  @Test
  public void test01() {
    AtomicInteger ato = new AtomicInteger(0);
    Integer value = RetryBuilder.<Integer>newBuilder().retryIfException().withMaxAttempt(3).build()
        .call(() -> {
          ato.incrementAndGet();
          int i = 0;
          System.out.println(10 / i);
          return ato.get();
        });
    Assert.assertNull(value);
    Assert.assertEquals(3, ato.get());
  }

  @Test
  public void test02() {
    AtomicInteger ato = new AtomicInteger(0);
    RetryBuilder.newBuilder().retryIfException(ArithmeticException.class).withMaxAttempt(4).build()
        .call(() -> {
          ato.incrementAndGet();
          int i = 0;
          System.out.println(10 / i);
          return ato.get();
        });
    Assert.assertEquals(4, ato.get());
  }

  @Test
  public void test03() {
    AtomicInteger ato = new AtomicInteger(0);
    RetryBuilder.<Integer>newBuilder().retryIfResult(rs -> rs < 3).build()
        .call(ato::incrementAndGet);
    Assert.assertEquals(3, ato.get());
  }

  @Test
  public void test04() {
    AtomicInteger ato = new AtomicInteger(0);
    RetryBuilder.<Integer>newBuilder().retryIfResult(rs -> rs < 3).withMaxAttempt(2).build()
        .call(ato::incrementAndGet);
    Assert.assertEquals(2, ato.get());
  }

  @Test
  public void test05() {
    long start = System.currentTimeMillis();
    AtomicInteger ato = new AtomicInteger(0);
    RetryBuilder.<Integer>newBuilder().retryIfResult(rs -> rs < 3).withSleepBlock(100).build()
        .call(ato::incrementAndGet);
    Assert.assertEquals(3, ato.get());
    Assert.assertTrue(System.currentTimeMillis() - start > 200);
  }

  @Test
  public void test06() {
    long start = System.currentTimeMillis();
    AtomicInteger ato = new AtomicInteger(0);
    RetryBuilder.<Integer>newBuilder().retryIfResult(rs -> rs < 3).withWaitBlock(100).build()
        .call(ato::incrementAndGet);
    Assert.assertEquals(3, ato.get());
    Assert.assertTrue(System.currentTimeMillis() - start > 200);
  }

  @Test
  public void test07() throws InterruptedException {
    AtomicInteger ato = new AtomicInteger(0);
    Retryer<Integer> retryer = RetryBuilder.<Integer>newBuilder().retryIfResult(rs -> rs < 3)
        .withSleepBlock(100).build();
    ThreadPoolExecutor pool = ThreadUtils.newThreadPool(2, 5, 100, 100, "xx-%d");
    Runnable r = () -> {
      System.out.println(Thread.currentThread());
      retryer.call(ato::incrementAndGet);
      System.out.println(Thread.currentThread());
    };
    pool.execute(r);
    pool.execute(r);

    ThreadUtils.sleep(1000);
    Assert.assertEquals(4, ato.get());
  }

  @Test
  public void test08() {
    AtomicInteger ato = new AtomicInteger(0);
    AtomicInteger fail = new AtomicInteger(0);
    RetryBuilder.<Integer>newBuilder().withTimeLimit(10, TimeUnit.MILLISECONDS)
        .retryIfException(TimeoutException.class).withMaxAttempt(3)
        .onFinalFail(r -> fail.incrementAndGet()).build().call(() -> {
      ato.incrementAndGet();
      ThreadUtils.sleep(20);
      if (!Thread.currentThread().isInterrupted()) {
        ato.incrementAndGet();
      }
      return 1;
    });
    Assert.assertEquals(1, fail.get());
  }

  @Test
  public void test09() {
    AtomicInteger ato = new AtomicInteger(0);
    RetryBuilder.<Integer>newBuilder().retryIfResult(rs -> rs < 3)
        .withBlock(r -> ThreadUtils.sleep(100)).build().call(ato::incrementAndGet);
    Assert.assertEquals(3, ato.get());
  }

  @Test
  public void test10() {
    AtomicInteger ato = new AtomicInteger(0);
    AtomicInteger retryAto = new AtomicInteger(0);
    AtomicInteger success = new AtomicInteger(0);
    RetryBuilder.<Integer>newBuilder().retryIfResult(rs -> rs < 3)
        .onRetry(r -> retryAto.incrementAndGet()).onSuccess(r -> success.incrementAndGet()).build()
        .call(ato::incrementAndGet);
    Assert.assertEquals(3, ato.get());
    Assert.assertEquals(3, retryAto.get());
    Assert.assertEquals(1, success.get());
  }

  @Test
  public void test11() {
    AtomicInteger ato = new AtomicInteger(0);
    RetryBuilder.<Integer>newBuilder().retryIfResult(rs -> rs < 31).withMaxDelay(500)
        .withBlock(r -> ThreadUtils.sleep(100)).build().call(ato::incrementAndGet);
    Assert.assertEquals(6, ato.get());
  }

  @Test
  public void test12() {
    AtomicInteger ato = new AtomicInteger(0);
    CompletableFuture<Integer> future = RetryBuilder.<Integer>newBuilder()
        .retryIfResult(rs -> rs < 3).buildAsync().callAsync(ato::incrementAndGet);
    future.thenAccept(r -> ato.incrementAndGet());
    future.join();
    ThreadUtils.sleep(100);
    Assert.assertEquals(4, ato.get());
  }
}