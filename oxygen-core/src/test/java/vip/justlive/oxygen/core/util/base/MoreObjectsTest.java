/*
 * Copyright (C) 2021 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

 class MoreObjectsTest {

  @Test
   void beanToMap() {

    Map<String, Object> source = new HashMap<>(4);
    source.put("1", 1);
    source.put("2", "@");
    source.put("3", MoreObjects.mapOf("1", 2));
    source.put("4", new Bean().setA(23));

    Map<String, Object> result = MoreObjects.beanToMap(source);
    assertEquals(source, result);

    result = MoreObjects.beanToMap(source, true);
    assertNotEquals(source, result);

    source.put("4", MoreObjects.mapOf("a", 23));
    assertEquals(source, result);
  }

  @Test
   void beanToProps() {
    Bean1 source = new Bean1().setA(1).setB(new Bean().setA(3));

    Properties props = MoreObjects.beanToProps(source);
    assertEquals("1", props.getProperty("a"));
    assertEquals("3", props.getProperty("b.a"));

    props = MoreObjects.beanToProps(MoreObjects.mapOf("a", "33", "s", source));
    assertEquals("1", props.getProperty("s.a"));
    assertEquals("3", props.getProperty("s.b.a"));

  }

  @Data
  @Accessors(chain = true)
  public static class Bean {

    private int a;

  }

  @Data
  @Accessors(chain = true)
  public static class Bean1 {

    private int a;

    private Bean b;

  }
}