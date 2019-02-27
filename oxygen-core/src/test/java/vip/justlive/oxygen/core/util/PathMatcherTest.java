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

    PathMatcher matcher = new PathMatcher();

    assertEquals("/a/b", matcher.getRootDir(pattern));
    assertTrue(matcher.match(pattern, "/a/b"));
    assertFalse(matcher.match(pattern, "/a/b/"));
    assertFalse(matcher.match(pattern, "/a/"));

    pattern = "/a/*";

    assertEquals("/a/", matcher.getRootDir(pattern));
    assertTrue(matcher.match(pattern, "/a/b"));
    assertTrue(matcher.match(pattern, "/a/"));
    assertFalse(matcher.match(pattern, "/"));
    assertFalse(matcher.match(pattern, "/a/b/c"));

    pattern = "/a/b?c";

    assertEquals("/a/", matcher.getRootDir(pattern));
    assertTrue(matcher.match(pattern, "/a/bac"));
    assertFalse(matcher.match(pattern, "/a/bc"));
    assertFalse(matcher.match(pattern, "/a/b"));
    assertFalse(matcher.match(pattern, "/a/bccc"));

    pattern = "/a/b*";

    assertEquals("/a/", matcher.getRootDir(pattern));
    assertTrue(matcher.match(pattern, "/a/b"));
    assertTrue(matcher.match(pattern, "/a/b1"));
    assertTrue(matcher.match(pattern, "/a/bv1"));
    assertFalse(matcher.match(pattern, "/a/b1/c"));
    assertFalse(matcher.match(pattern, "/a/b/c"));

    pattern = "/a/b*";

    assertTrue(matcher.match(pattern, "/a/b"));
    assertTrue(matcher.match(pattern, "/a/b1"));
    assertTrue(matcher.match(pattern, "/a/bv1"));
    assertFalse(matcher.match(pattern, "/a/c"));
    assertFalse(matcher.match(pattern, "/a/b1/c"));
    assertFalse(matcher.match(pattern, "/a/b/c"));

    pattern = "/a/b**";

    assertTrue(matcher.match(pattern, "/a/b"));
    assertTrue(matcher.match(pattern, "/a/b1"));
    assertTrue(matcher.match(pattern, "/a/bv1"));
    assertFalse(matcher.match(pattern, "/a/c"));
    assertTrue(matcher.match(pattern, "/a/b1/c"));
    assertTrue(matcher.match(pattern, "/a/b/c"));

    pattern = "/a/**/a";

    assertEquals("/a/", matcher.getRootDir(pattern));
    assertTrue(matcher.match(pattern, "/a/a"));
    assertTrue(matcher.match(pattern, "/a/1/a"));

    pattern = "/*";
    assertEquals("/", matcher.getRootDir(pattern));

  }

}