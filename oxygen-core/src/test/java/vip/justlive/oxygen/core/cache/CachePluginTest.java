/*
 * Copyright (C) 2018 justlive1
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
package vip.justlive.oxygen.core.cache;

import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.ioc.BeanStore;

/**
 * @author wubo
 */
public class CachePluginTest {

  @Test
  public void test() throws InterruptedException {

    Bootstrap.start();

    CacheService cacheService = BeanStore.getBean(CacheService.class);
    cacheService.time();
    cacheService.time();
    cacheService.time1(1);
    cacheService.time1(2);
    cacheService.time1(2);

    Assert.assertEquals(3, JCache.cache().keys().size());

    TimeUnit.SECONDS.sleep(1);

    cacheService.time1(1);
    Assert.assertEquals(2, JCache.cache().keys().size());
  }
}