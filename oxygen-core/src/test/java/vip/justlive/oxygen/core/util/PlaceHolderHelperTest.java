package vip.justlive.oxygen.core.util;

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;

/**
 * @author wubo
 */
public class PlaceHolderHelperTest {

  Properties props = new Properties();

  PlaceHolderHelper helper = new PlaceHolderHelper(PlaceHolderHelper.DEFAULT_PLACEHOLDER_PREFIX,
      PlaceHolderHelper.DEFAULT_PLACEHOLDER_SUFFIX, PlaceHolderHelper.DEFAULT_VALUE_SEPARATOR,
      true);

  @Before
  public void before() {

    props.put("a", "a");
    props.put("b", "${a}");
    props.put("c", "${b}");
    props.put("d", "${c}");
    props.put("e", "${f:${d}}");

  }

  @Test
  public void testHelper() {

    assertEquals("a", helper.replacePlaceholders(props.getProperty("a"), props));
    assertEquals("a", helper.replacePlaceholders(props.getProperty("b"), props));
    assertEquals("a", helper.replacePlaceholders(props.getProperty("c"), props));
    assertEquals("a", helper.replacePlaceholders(props.getProperty("d"), props));
    assertEquals("a", helper.replacePlaceholders(props.getProperty("e"), props));
  }
}