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

import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.util.base.SnowflakeId.Cfg;

/**
 * @author wubo
 */
class SnowflakeIdTest {


  @Test
  void test() {
    SnowflakeId.defaultNextId();

    long curr = System.currentTimeMillis();
    long id = SnowflakeId.defaultNextId();
    assertEquals(curr, SnowflakeId.defaultInstance().getCreatedAt(id));

    SnowflakeId worker = new SnowflakeId(3, 12);
    id = worker.nextId();
    assertEquals(3, worker.getWorkerId(id));
    assertEquals(12, worker.getDataCenterId(id));

    worker = new SnowflakeId(new Cfg().setWorkerId(1).setWorkerIdBits(4).setDataCenterIdBits(1));
    System.out.println(worker.nextId());

  }

}