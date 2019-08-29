/*
 * Copyright (C) 2019 the original author or authors.
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
package vip.justlive.oxygen.cache;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import vip.justlive.oxygen.cache.store.JCache;
import vip.justlive.oxygen.core.Bootstrap;

/**
 * @author wubo
 */
public class JCacheTest {

  @Test
  public void test() {

    Bootstrap.start();

    JCache.cache("z").putIfAbsent("k", 1);
    assertEquals(1, JCache.cache("z").get("k"));

  }

}