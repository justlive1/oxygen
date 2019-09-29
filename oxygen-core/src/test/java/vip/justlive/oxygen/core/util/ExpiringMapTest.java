/*
 * Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */

package vip.justlive.oxygen.core.util;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.util.ExpiringMap.CleanPolicy;
import vip.justlive.oxygen.core.util.ExpiringMap.ExpiringPolicy;

public class ExpiringMapTest {

  @Test
  public void test1() throws Exception {

    ExpiringMap<String, Integer> expiringMap = ExpiringMap.<String, Integer>builder()
        // 默认失效时间 50
        .expiration(50, TimeUnit.MILLISECONDS)
        // 累积4次
        .accumulateThreshold(4).build();

    String key = "key";
    expiringMap.put(key, 1);

    Assert.assertNotNull(expiringMap.get(key));

    TimeUnit.MILLISECONDS.sleep(60);

    Assert.assertNull(expiringMap.get(key));

    expiringMap.get(key);
    expiringMap.get(key);
    expiringMap.get(key);

    // 等待清理线程执行完毕
    TimeUnit.MILLISECONDS.sleep(10);

    Assert.assertEquals(0, expiringMap.realSize());

  }

  @Test
  public void test2() throws Exception {

    ExpiringMap<String, Integer> expiringMap = ExpiringMap.<String, Integer>builder()
        // 默认失效时间 20
        .expiration(20, TimeUnit.MILLISECONDS)
        // 访问刷新
        .expiringPolicy(ExpiringPolicy.ACCESSED)
        // 累积
        .accumulateThreshold(4).build();

    String key = "key";
    String k = "k";
    expiringMap.put(key, 1);
    expiringMap.put(k, 1);

    Assert.assertNotNull(expiringMap.get(key));
    Assert.assertNotNull(expiringMap.get(k));

    TimeUnit.MILLISECONDS.sleep(10);

    Assert.assertNotNull(expiringMap.get(key));

    TimeUnit.MILLISECONDS.sleep(10);

    Assert.assertNotNull(expiringMap.get(key));

    TimeUnit.MILLISECONDS.sleep(10);

    Assert.assertNotNull(expiringMap.get(key));
    Assert.assertNull(expiringMap.get(k));

  }

  @Test
  public void test3() throws Exception {

    ExpiringMap<String, Integer> expiringMap = ExpiringMap.<String, Integer>builder()
        // 默认失效时间 20
        .expiration(20, TimeUnit.MILLISECONDS)
        // 定时清理任务
        .cleanPolicy(CleanPolicy.SCHEDULE)
        // 定时任务间隔
        .scheduleDelay(1)
        // 累积
        .accumulateThreshold(4).build();

    String key = "key";
    expiringMap.put(key, 1);

    Assert.assertNotNull(expiringMap.get(key));

    TimeUnit.MILLISECONDS.sleep(30);

    Assert.assertNull(expiringMap.get(key));
    Assert.assertEquals(1, expiringMap.realSize());

    TimeUnit.MILLISECONDS.sleep(1000);

    Assert.assertEquals(0, expiringMap.realSize());

  }

  @Test
  public void test4() throws Exception {

    ExpiringMap<String, Integer> expiringMap = ExpiringMap.<String, Integer>builder()
        // 默认失效时间 10
        .expiration(10, TimeUnit.MILLISECONDS)
        // 累积4次
        .accumulateThreshold(4).build();

    String key = "key";
    String k = "k";
    expiringMap.put(key, 1, 20, TimeUnit.MILLISECONDS);
    expiringMap.put(k, 1);

    Assert.assertNotNull(expiringMap.get(key));
    Assert.assertNotNull(expiringMap.get(k));

    TimeUnit.MILLISECONDS.sleep(15);

    Assert.assertNull(expiringMap.get(k));
    Assert.assertNotNull(expiringMap.get(key));

    TimeUnit.MILLISECONDS.sleep(10);

    Assert.assertNull(expiringMap.get(key));

  }

  @Test
  public void test5() throws InterruptedException {
    List<String> list = new ArrayList<>();
    ExpiringMap<String, Integer> expiringMap = ExpiringMap.<String, Integer>builder()
        // 默认失效时间 10
        .expiration(30, TimeUnit.MILLISECONDS)
        // 累积1次
        .accumulateThreshold(1).maxSize(2).asyncExpiredListeners((k, v) -> list.add(k)).build();

    expiringMap.put("1", 1);
    expiringMap.put("2", 2);
    TimeUnit.MILLISECONDS.sleep(25);
    expiringMap.put("3", 3);
    TimeUnit.MILLISECONDS.sleep(10);
    expiringMap.get("3");
    TimeUnit.MILLISECONDS.sleep(20);

    Assert.assertEquals(2, list.size());
    Assert.assertEquals("1", list.get(0));
    Assert.assertEquals("2", list.get(1));


  }

}