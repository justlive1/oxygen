package vip.justlive.oxygen.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import org.junit.Test;

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