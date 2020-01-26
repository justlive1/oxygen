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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author wubo
 */
public class SnowflakeIdWorkerTest {


  @Test
  public void test() {
    SnowflakeIdWorker.defaultNextId();

    long curr = System.currentTimeMillis();
    long id = SnowflakeIdWorker.defaultNextId();
    Assert.assertEquals(curr, SnowflakeIdWorker.getCreatedAt(id));

    SnowflakeIdWorker worker = new SnowflakeIdWorker(3, 12);
    id = worker.nextId();
    Assert.assertEquals(3, SnowflakeIdWorker.getWorkerId(id));
    Assert.assertEquals(12, SnowflakeIdWorker.getDataCenterId(id));
  }

}