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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.util.base.MoreObjects;

/**
 * 一个简单的公式计算器
 *
 * @author wubo
 */
@UtilityClass
public class Calculator {

  private final String TEMPLATE = "#{var $result = %s; $out.write('' + $result); }";

  /**
   * 计算
   *
   * @param formula 公式
   * @return 结果
   */
  public BigDecimal calculate(String formula) {
    return calculate(formula, null);
  }

  /**
   * 计算
   *
   * @param formula 公式
   * @param data 变量参数
   * @return 结果
   */
  public BigDecimal calculate(String formula, Object data) {
    Map<String, Object> attrs = new HashMap<>(4);
    if (data != null) {
      attrs.putAll(MoreObjects.beanToMap(data));
    }
    return new BigDecimal(Templates.render(String.format(TEMPLATE, formula), attrs));
  }

  /**
   * 计算
   *
   * @param formula 公式
   * @param data 变量参数
   * @param scale 精度
   * @return 结果
   */
  public BigDecimal calculate(String formula, Object data, int scale) {
    return calculate(formula, data).setScale(scale, RoundingMode.HALF_DOWN);
  }
}
