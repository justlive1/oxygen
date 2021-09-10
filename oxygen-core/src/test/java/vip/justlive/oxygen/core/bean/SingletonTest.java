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
package vip.justlive.oxygen.core.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * @author wubo
 */
class SingletonTest {

  @Test
  void test() {
    Singleton.set("echo0", new EchoImpl0(), 0);
    Singleton.set("echo1", new EchoImpl1(), 1);

    assertTrue(Singleton.get("echo0") instanceof EchoImpl0);
    assertNotNull(Singleton.get(Echo0.class));
    assertNotNull(Singleton.get(EchoImpl0.class));
    assertNotNull(Singleton.get("echo0", Echo0.class));

    assertEquals(2, Singleton.getMap(Echo0.class).size());
    assertEquals(1, Singleton.getMap(Echo1.class).size());
    assertEquals(2, Singleton.getMap(EchoImpl0.class).size());
    assertEquals(2, Singleton.getCastMap(Echo0.class).size());
    assertEquals(1, Singleton.getCastMap(Echo1.class).size());
    assertEquals(2, Singleton.getCastMap(EchoImpl0.class).size());

    assertEquals(2, Singleton.getList(Echo0.class).size());
    assertEquals(1, Singleton.getList(Echo1.class).size());
    assertEquals(2, Singleton.getList(EchoImpl0.class).size());

    Singleton.set("echo2", new EchoImpl0(), 0);
    try {
      Singleton.get(Echo0.class);
      fail();
    } catch (Exception e) {
      // ignore
    }

    Singleton.clear();
    assertEquals(0, Singleton.names().size());
  }
}