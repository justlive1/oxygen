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

import org.junit.Assert;
import org.junit.Test;

public class DecimalSystemConvertTest {

  @Test
  public void convert() {
    int value = 1233;
    Assert.assertEquals(Integer.toOctalString(value), DecimalSystemConvert.convert(value, 8));
    Assert.assertEquals(Integer.toHexString(value), DecimalSystemConvert.convert(value, 16));
  }

  @Test
  public void recover() {
    int value = 2131244;
    String result = Integer.toHexString(value);
    int rs = DecimalSystemConvert.recover(result, 16);
    Assert.assertEquals(value, rs);
  }
}