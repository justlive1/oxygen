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