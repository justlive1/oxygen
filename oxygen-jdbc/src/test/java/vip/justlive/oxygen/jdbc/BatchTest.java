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
package vip.justlive.oxygen.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.jdbc.record.Entity;

/**
 * @author wubo
 */
class BatchTest {

  @BeforeEach
  void before() {
    Bootstrap.start();
    Jdbc.update("create table system (id int primary key auto_increment, key int, value int);");
  }

  @Test
  void test() {
    Batch.use().addBatch("insert into system (key, value) values (?,?)", 1, 3)
        .addBatch("insert into system (key, value) values (?,?)", Arrays.asList(4, 4))
        .addBatch("insert into system (key,value) values(1,2)")
        .addBatch("insert into system (key,value) values(2,2)").commit();

    List<Map<String, Object>> map = Jdbc.queryForMapList("select * from system");
    assertEquals(4, map.size());

    List<System> list = new ArrayList<>();
    System system = new System();
    system.setValue(1);
    list.add(system);
    system = new System();
    system.setValue(2);
    list.add(system);
    system = new System();
    system.setKey(3);
    list.add(system);
    system = new System();
    system.setKey(3);
    system.setValue(4);
    list.add(system);

    Entity.parse(System.class).insertBatch(list);
    map = Jdbc.queryForMapList("select * from system");
    assertEquals(8, map.size());
  }

}