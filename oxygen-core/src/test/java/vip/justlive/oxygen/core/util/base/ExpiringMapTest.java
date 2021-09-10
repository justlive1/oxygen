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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.util.base.ExpiringMap.CleanPolicy;
import vip.justlive.oxygen.core.util.base.ExpiringMap.ExpiringPolicy;
import vip.justlive.oxygen.core.util.base.ExpiringMap.RemovalCause;

class ExpiringMapTest {

  @Test
  void test1() throws Exception {

    ExpiringMap<String, Integer> expiringMap = ExpiringMap.<String, Integer>builder()
        // 默认失效时间 50
        .expiration(500, TimeUnit.MILLISECONDS)
        // 累积4次
        .accumulateThreshold(4).build();

    String key = "key";
    expiringMap.put(key, 1);

    assertNotNull(expiringMap.get(key));

    TimeUnit.MILLISECONDS.sleep(600);

    assertNull(expiringMap.get(key));

    expiringMap.get(key);
    expiringMap.get(key);
    expiringMap.get(key);

    // 等待清理线程执行完毕
    TimeUnit.MILLISECONDS.sleep(100);

    assertEquals(0, expiringMap.realSize());

  }

  @Test
  void test2() throws Exception {

    ExpiringMap<String, Integer> expiringMap = ExpiringMap.<String, Integer>builder()
        // 默认失效时间 20
        .expiration(200, TimeUnit.MILLISECONDS)
        // 访问刷新
        .expiringPolicy(ExpiringPolicy.ACCESSED)
        // 累积
        .accumulateThreshold(4).build();

    String key = "key";
    String k = "k";
    expiringMap.put(key, 1);
    expiringMap.put(k, 1);

    assertNotNull(expiringMap.get(key));
    assertNotNull(expiringMap.get(k));

    TimeUnit.MILLISECONDS.sleep(100);

    assertNotNull(expiringMap.get(key));

    TimeUnit.MILLISECONDS.sleep(100);

    assertNotNull(expiringMap.get(key));

    TimeUnit.MILLISECONDS.sleep(100);

    assertNotNull(expiringMap.get(key));
    assertNull(expiringMap.get(k));

  }

  @Test
  void test3() throws Exception {

    ExpiringMap<String, Integer> expiringMap = ExpiringMap.<String, Integer>builder()
        // 默认失效时间 20
        .expiration(200, TimeUnit.MILLISECONDS)
        // 定时清理任务
        .cleanPolicy(CleanPolicy.SCHEDULE)
        // 定时任务间隔
        .scheduleDelay(1)
        // 累积
        .accumulateThreshold(4).build();

    String key = "key";
    expiringMap.put(key, 1);

    assertNotNull(expiringMap.get(key));

    TimeUnit.MILLISECONDS.sleep(300);

    assertNull(expiringMap.get(key));
    assertEquals(1, expiringMap.realSize());

    TimeUnit.MILLISECONDS.sleep(1000);

    assertEquals(0, expiringMap.realSize());

  }

  @Test
  void test4() throws Exception {

    ExpiringMap<String, Integer> expiringMap = ExpiringMap.<String, Integer>builder()
        // 默认失效时间 10
        .expiration(100, TimeUnit.MILLISECONDS)
        // 累积4次
        .accumulateThreshold(4).build();

    String key = "key";
    String k = "k";
    expiringMap.put(key, 1, 200, TimeUnit.MILLISECONDS);
    expiringMap.put(k, 1);

    assertNotNull(expiringMap.get(key));
    assertNotNull(expiringMap.get(k));

    TimeUnit.MILLISECONDS.sleep(150);

    assertNull(expiringMap.get(k));
    assertNotNull(expiringMap.get(key));

    TimeUnit.MILLISECONDS.sleep(100);

    assertNull(expiringMap.get(key));

  }

  @Test
  void test5() throws InterruptedException {
    List<String> list = new ArrayList<>();
    List<String> size = new ArrayList<>();
    ExpiringMap<String, Integer> expiringMap = ExpiringMap.<String, Integer>builder()
        // 默认失效时间 10
        .expiration(300, TimeUnit.MILLISECONDS)
        // 累积1次
        .accumulateThreshold(1).maxSize(2).asyncExpiredListeners((k, v, cause) -> {
          list.add(k);
          if (cause == RemovalCause.SIZE) {
            size.add(k);
          }
        })
        .build();

    expiringMap.put("1", 1);
    expiringMap.put("2", 2);
    TimeUnit.MILLISECONDS.sleep(250);
    expiringMap.put("3", 3);
    TimeUnit.MILLISECONDS.sleep(100);
    expiringMap.get("3");
    TimeUnit.MILLISECONDS.sleep(200);

    assertEquals(2, list.size());
    assertEquals("1", list.get(0));
    assertEquals("2", list.get(1));

    assertEquals(1, size.size());
    assertEquals("1", size.get(0));
  }

}
