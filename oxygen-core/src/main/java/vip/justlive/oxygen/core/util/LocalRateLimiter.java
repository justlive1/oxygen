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

package vip.justlive.oxygen.core.util;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 本地限流实现
 *
 * @author wubo
 */
public class LocalRateLimiter implements RateLimiter {

  private static final ScheduledExecutorService POOL = ThreadUtils
      .newScheduledExecutor(10, "rate-limiter-%d");

  private Map<String, Resource> resources = new ConcurrentHashMap<>(4);

  @Override
  public void setRate(String key, long rate, long interval) {
    if (!resources.containsKey(key)) {
      resources.putIfAbsent(key, new Resource(rate, interval));
    }
  }

  @Override
  public boolean tryAcquire(String key, long permits) {
    if (permits > get(key).rate) {
      throw Exceptions.fail("Requested permits amount could not exceed defined rate");
    }
    return sync(key, permits) == null;
  }

  @Override
  public boolean tryAcquire(String key, long permits, long timeout, TimeUnit unit) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    tryAcquireAsync(key, permits, unit.toMillis(timeout), future);
    return future.join();
  }

  @Override
  public void acquire(String key, long permits) {
    tryAcquire(key, permits, 0, TimeUnit.MILLISECONDS);
  }

  private void tryAcquireAsync(String key, long permits, long timeout,
      CompletableFuture<Boolean> future) {
    long current = System.currentTimeMillis();
    CompletableFuture.supplyAsync(() -> sync(key, permits), POOL).whenComplete((res, e) -> {
      if (e != null) {
        future.completeExceptionally(e);
        return;
      }
      if (res == null) {
        future.complete(Boolean.TRUE);
        return;
      }
      if (timeout <= 0) {
        POOL.schedule(() -> tryAcquireAsync(key, permits, timeout, future), res,
            TimeUnit.MILLISECONDS);
        return;
      }
      long el = System.currentTimeMillis() - current;
      long remains = timeout - el;
      // 已超时
      if (remains <= 0) {
        future.complete(Boolean.FALSE);
        return;
      }
      // 剩余时间不够
      if (remains < res) {
        POOL.schedule(() -> future.complete(Boolean.FALSE), remains, TimeUnit.MILLISECONDS);
        return;
      }
      // 等待时间窗口过去后重试
      long start = System.currentTimeMillis();
      POOL.schedule(() -> {
        long elapsed = System.currentTimeMillis() - start;
        if (remains <= elapsed) {
          future.complete(Boolean.FALSE);
          return;
        }
        tryAcquireAsync(key, permits, remains - elapsed, future);
      }, res, TimeUnit.MILLISECONDS);
    });
  }

  protected Long sync(String key, long permits) {
    return get(key).sync(permits);
  }

  private Resource get(String key) {
    Resource resource = resources.get(key);
    if (resource == null) {
      throw Exceptions.fail("RateLimiter is not initialized");
    }
    return resource;
  }

  @Data
  private static class Resource {

    private final long rate;
    private final long interval;
    private long start;
    private long state;

    synchronized Long sync(long permits) {
      long now = System.currentTimeMillis();
      long res = now - start;
      if (res > interval) {
        start = now;
        state = 0;
        res = 0;
      }
      if (state + permits <= rate) {
        state += permits;
        return null;
      }
      return interval - res;
    }
  }
}
