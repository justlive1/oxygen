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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author wubo
 */
class SplitterMatcherTest {

  @Test
  void test() {
    SplitterMatcher matcher = new SplitterMatcher('.');

    String pattern = ".a.b";

    assertTrue(matcher.match(pattern, ".a.b"));
    assertFalse(matcher.match(pattern, ".a.b."));
    assertFalse(matcher.match(pattern, ".a."));

    pattern = ".a.*";

    assertTrue(matcher.match(pattern, ".a.b"));
    assertTrue(matcher.match(pattern, ".a."));
    assertFalse(matcher.match(pattern, "."));
    assertFalse(matcher.match(pattern, ".a.b.c"));

    pattern = ".a.b?c";

    assertTrue(matcher.match(pattern, ".a.bac"));
    assertFalse(matcher.match(pattern, ".a.bc"));
    assertFalse(matcher.match(pattern, ".a.b"));
    assertFalse(matcher.match(pattern, ".a.bccc"));

    pattern = ".a.b*";

    assertTrue(matcher.match(pattern, ".a.b"));
    assertTrue(matcher.match(pattern, ".a.b1"));
    assertTrue(matcher.match(pattern, ".a.bv1"));
    assertFalse(matcher.match(pattern, ".a.b1.c"));
    assertFalse(matcher.match(pattern, ".a.b.c"));
    assertFalse(matcher.match(pattern, ".a.c"));

    pattern = ".a.b**";

    assertTrue(matcher.match(pattern, ".a.b"));
    assertTrue(matcher.match(pattern, ".a.b1"));
    assertTrue(matcher.match(pattern, ".a.bv1"));
    assertFalse(matcher.match(pattern, ".a.c"));
    assertTrue(matcher.match(pattern, ".a.b1.c"));
    assertTrue(matcher.match(pattern, ".a.b.c"));

    pattern = ".a.**.a";

    assertTrue(matcher.match(pattern, ".a.a"));
    assertTrue(matcher.match(pattern, ".a.1.a"));

  }
}