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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

/**
 * @author wubo
 */
public class PathMatcherTest {

  @Test
  public void test() throws URISyntaxException {

    String pattern = "/a/b";

    assertEquals("/a/b", PathMatcher.getRootDir(pattern));
    assertTrue(PathMatcher.match(pattern, "/a/b"));
    assertFalse(PathMatcher.match(pattern, "/a/b/"));
    assertFalse(PathMatcher.match(pattern, "/a/"));

    pattern = "/a/*";

    assertEquals("/a/", PathMatcher.getRootDir(pattern));
    assertTrue(PathMatcher.match(pattern, "/a/b"));
    assertTrue(PathMatcher.match(pattern, "/a/"));
    assertFalse(PathMatcher.match(pattern, "/"));
    assertFalse(PathMatcher.match(pattern, "/a/b/c"));

    pattern = "/a/b?c";

    assertEquals("/a/", PathMatcher.getRootDir(pattern));
    assertTrue(PathMatcher.match(pattern, "/a/bac"));
    assertFalse(PathMatcher.match(pattern, "/a/bc"));
    assertFalse(PathMatcher.match(pattern, "/a/b"));
    assertFalse(PathMatcher.match(pattern, "/a/bccc"));

    pattern = "/a/b*";

    assertEquals("/a/", PathMatcher.getRootDir(pattern));
    assertTrue(PathMatcher.match(pattern, "/a/b"));
    assertTrue(PathMatcher.match(pattern, "/a/b1"));
    assertTrue(PathMatcher.match(pattern, "/a/bv1"));
    assertFalse(PathMatcher.match(pattern, "/a/b1/c"));
    assertFalse(PathMatcher.match(pattern, "/a/b/c"));
    assertFalse(PathMatcher.match(pattern, "/a/c"));

    pattern = "/a/b**";

    assertTrue(PathMatcher.match(pattern, "/a/b"));
    assertTrue(PathMatcher.match(pattern, "/a/b1"));
    assertTrue(PathMatcher.match(pattern, "/a/bv1"));
    assertFalse(PathMatcher.match(pattern, "/a/c"));
    assertTrue(PathMatcher.match(pattern, "/a/b1/c"));
    assertTrue(PathMatcher.match(pattern, "/a/b/c"));

    pattern = "/a/**/a";

    assertEquals("/a/", PathMatcher.getRootDir(pattern));
    assertTrue(PathMatcher.match(pattern, "/a/a"));
    assertTrue(PathMatcher.match(pattern, "/a/1/a"));

    pattern = "/*";
    assertEquals("/", PathMatcher.getRootDir(pattern));

  }

}