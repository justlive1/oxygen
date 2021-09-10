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
package vip.justlive.oxygen.core.util.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * @author wubo
 */
class CsvWriterTest {

  @Test
  void test() {

    CsvWriter writer = new CsvWriter();
    StringBuilder sb = new StringBuilder();

    writer.write(sb, new String[]{"a", "b"}, false);
    assertEquals("a,b\r\n", sb.toString());

    writer.write(sb, Arrays.asList("a", "b"));
    assertEquals("a,b\r\n\"a\",\"b\"\r\n", sb.toString());

    sb.setLength(0);
    writer.write(sb, Arrays.asList("a", "b,c"));
    assertEquals("\"a\",\"b,c\"\r\n", sb.toString());

    sb.setLength(0);
    writer.writeAll(sb, Arrays.asList(new String[]{"1", "2"}, new String[]{"3", null, "5"}), false);
    assertEquals("1,2\r\n3,,5\r\n", sb.toString());

    sb.setLength(0);
    writer.writeAll(sb, Arrays.asList(new String[]{"1", "2"}, new String[]{"3"}, null));
    assertEquals("\"1\",\"2\"\r\n\"3\"\r\n", sb.toString());

  }
}