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

import org.junit.jupiter.api.Test;


/**
 * @author wubo
 */
class SensitiveUtilsTest {

  @Test
  void nameHide() {
    assertEquals("赵**", SensitiveUtils.nameHide("赵倩"));
    assertEquals("赵**", SensitiveUtils.nameHide("赵倩倩"));
  }

  @Test
  void idCardNoHide() {
    assertEquals("11**************97", SensitiveUtils.idCardNoHide("110101199003072797"));
  }

  @Test
  void bankCardNoHide() {
    assertEquals("330104*********2007", SensitiveUtils.bankCardNoHide("3301040160000852007"));
    assertEquals("33010*****0008", SensitiveUtils.bankCardNoHide("33010401600008"));
  }

  @Test
  public void cellphoneHide() {
    assertEquals("130******58", SensitiveUtils.cellphoneHide("13071835358"));
    assertEquals("307****358", SensitiveUtils.cellphoneHide("3071835358"));
    assertEquals("07****358", SensitiveUtils.cellphoneHide("071835358"));
    assertEquals("8****8", SensitiveUtils.cellphoneHide("835358"));
  }

  @Test
  public void emailHide() {
    assertEquals("835***@163.com", SensitiveUtils.emailHide("835358@163.com"));
    assertEquals("83***@163.com", SensitiveUtils.emailHide("83@163.com"));
  }

  @Test
  public void defaultHide() {
    assertEquals("t*t", SensitiveUtils.defaultHide("ttt"));
    assertEquals("123***78", SensitiveUtils.defaultHide("12345678"));
  }

  @Test
  public void customizeHide() {
    assertEquals("135****4561", SensitiveUtils.customizeHide("13568794561", 3, 4, 4));
    assertEquals("****4561", SensitiveUtils.customizeHide("13568794561", 0, 4, 4));
    assertEquals("135****", SensitiveUtils.customizeHide("13568794561", 3, 0, 4));
    assertEquals("135********", SensitiveUtils.customizeHide("13568794561", 3, 0, 8));
  }
}
