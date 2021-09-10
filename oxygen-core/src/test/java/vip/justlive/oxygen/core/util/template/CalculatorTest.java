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

package vip.justlive.oxygen.core.util.template;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.util.base.MoreObjects;

/**
 * @author wubo
 */
class CalculatorTest {

  @Test
  void calculate() {
    String formula = "2 * 4 + 3";
    BigDecimal value = Calculator.calculate(formula);
    assertEquals(new BigDecimal(11), value);
  }

  @Test
  void calculate1() {
    String formula = "2 * a + 3 * b - c";
    BigDecimal value = Calculator.calculate(formula, MoreObjects.mapOf("a", 3, "b", 4.2, "c", 2.5));
    assertEquals(new BigDecimal(2).multiply(new BigDecimal(3))
            .add(new BigDecimal(3).multiply(new BigDecimal("4.2")).subtract(new BigDecimal("2.5"))),
        value);
  }

  @Test
  void calculate2() {
    String formula = "3 * b / c";
    BigDecimal value = Calculator
        .calculate(formula, MoreObjects.mapOf("b", 4.2, "c", 2.3), 2);
    assertEquals(new BigDecimal("3")
            .multiply(new BigDecimal("4.2").divide(new BigDecimal("2.3"), 5, RoundingMode.HALF_DOWN))
            .setScale(2, RoundingMode.HALF_DOWN),
        value);
  }
}