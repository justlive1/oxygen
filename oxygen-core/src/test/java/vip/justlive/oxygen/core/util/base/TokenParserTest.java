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

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wubo
 */
public class TokenParserTest {

  @Test
  public void parse() {
    TokenParser parser = new TokenParser("${", "}", this::handle);
    String value = parser.parse(null);
    assertEquals("", value);

    value = parser.parse("ax");
    assertEquals("ax", value);

    value = parser.parse("ax${a}");
    assertEquals("axA", value);

    value = parser.parse("ax${a}}");
    assertEquals("axA}", value);

    value = parser.parse("ax${a\\}x}");
    assertEquals("axA}X", value);

    value = parser.parse("ax\\${ax}${ax}");
    assertEquals("ax${ax}AX", value);

    value = parser.parse("ax$\\{ax}${ax}");
    assertEquals("ax$\\{ax}AX", value);

    value = parser.parse("ax$${ax}${ax}");
    assertEquals("ax$AXAX", value);

  }

  private String handle(String text) {
    return text.toUpperCase();
  }
}