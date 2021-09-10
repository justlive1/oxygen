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

package vip.justlive.oxygen.core.util.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Data;
import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.util.base.MoreObjects;

/**
 * @author wubo
 */
class JsonWriterTest {

  @Test
  void toJson() {

    // Primitive
    assertEquals("1", Json.toJson(1));
    assertEquals("23.3", Json.toJson(23.3f));
    assertEquals("9024512353132414", Json.toJson(9024512353132414L));
    assertEquals("true", Json.toJson(true));

    assertEquals("null", Json.toJson(null));

    Date date = new Date();
    assertEquals(Long.toString(date.getTime()), Json.toJson(date));
    LocalDateTime time = LocalDateTime.now();
    assertEquals("\"" + time.toString() + "\"", Json.toJson(time));

    Bean bean = new Bean();
    bean.a = 1;
    bean.b = true;
    bean.c = time;

    assertEquals("{\"a\":1,\"b\":true,\"c\":\"" + time.toString() + "\",\"d\":null}",
        Json.toJson(bean));

    Map<String, Object> map = MoreObjects.mapOf("a", 1, "b", true, "c", time);
    assertEquals("{\"a\":1,\"b\":true,\"c\":\"" + time.toString() + "\"}", Json.toJson(map));

    List<Object> list = Arrays.asList(1, 2, 3, bean);
    assertEquals("[1,2,3,{\"a\":1,\"b\":true,\"c\":\"" + time.toString() + "\",\"d\":null}]",
        Json.toJson(list));

    int[] array = new int[]{3, 4, 5};
    assertEquals("[3,4,5]", Json.toJson(array));

    assertEquals("\"UTF-8\"", Json.toJson(StandardCharsets.UTF_8));
    assertEquals("\"Asia/Shanghai\"", Json.toJson(ZoneId.of("Asia/Shanghai")));
    assertEquals("1", Json.toJson(new AtomicReference<>(1)));
    assertEquals("[1,2]", Json.toJson(new AtomicIntegerArray(new int[]{1, 2})));

  }

  @Data
  public static class Bean {

    private int a;
    private boolean b;
    private LocalDateTime c;
    private Date d;
  }
}
