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

package vip.justlive.oxygen.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.Data;
import org.junit.jupiter.api.Test;

/**
 * @author wubo
 */
class ConfigFactoryTest {

  @Test
  void testLoadOneProp() {

    ConfigFactory.loadProperties("classpath*:config.properties", "classpath*:/config/*.properties");

    assertTrue(ConfigFactory.keys().contains("fc.age"));

    Prop prop = ConfigFactory.load(Prop.class);

    assertNotNull(prop);
    assertEquals("jack", prop.getName());

    assertEquals(new Integer(19), prop.getAge());

    System.setProperty("fc.age", "23");
    ConfigFactory.setProperty("fc.name", "321");

    prop = ConfigFactory.load(Prop.class);
    assertEquals(new Integer(23), prop.getAge());

    ConfigFactory.setProperty("fc.age", "32");
    prop = ConfigFactory.load(Prop.class);

    assertEquals(new Integer(32), prop.getAge());
  }

  @Data
  @ValueConfig("fc")
  static class Prop {

    @Value("${fc.name}")
    private String name;

    private Integer age;
  }

}