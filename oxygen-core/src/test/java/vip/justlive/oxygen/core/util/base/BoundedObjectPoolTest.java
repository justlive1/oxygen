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
package vip.justlive.oxygen.core.util.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * @author wubo
 */
@Slf4j
public class BoundedObjectPoolTest {

  AtomicLong c = new AtomicLong();

  @Test
  public void test() throws InterruptedException {
    BoundedObjectPool<Entity> pool = new BoundedObjectPool<>(2, this::create);

    List<Callable<Integer>> list = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      list.add(() -> {
        pool.run(a -> {
          log.info("run {}", a);
        });
        return null;
      });
    }

    Executors.newFixedThreadPool(10).invokeAll(list);

    ThreadUtils.sleep(1000);
    int size = ((List<?>) ClassUtils.getValue(pool, "pool")).size();

    Assert.assertEquals(2, size);
  }

  private Entity create() {
    return new Entity(c.getAndIncrement());
  }

  @ToString
  @RequiredArgsConstructor
  static class Entity {

    final long a;
  }
}
