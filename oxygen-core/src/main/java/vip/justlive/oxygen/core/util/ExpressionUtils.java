/*
 * Copyright (C) 2019 the original author or authors.
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
package vip.justlive.oxygen.core.util;

import java.util.Map;
import lombok.experimental.UtilityClass;

/**
 * EL表达式工具
 *
 * @author wubo
 */
@UtilityClass
public class ExpressionUtils {

  private static final String REGEX = "[.]";

  /**
   * 获取EL的值
   *
   * @param source 数据源
   * @param expression ${expression} exclude ${}
   * @return value
   */
  public static Object eval(Object source, String expression) {
    if (source == null || expression == null || expression.length() == 0) {
      return null;
    }
    Object value = source;
    for (String field : expression.split(REGEX)) {
      value = getValue(value, field);
    }
    return value;
  }

  /**
   * 获取值
   *
   * @param source 数据源
   * @param name 字段名
   * @return value
   */
  private static Object getValue(Object source, String name) {
    MoreObjects.notNull(source);
    MoreObjects.notNull(name);
    if (Map.class.isAssignableFrom(source.getClass())) {
      return ((Map<?, ?>) source).get(name);
    }
    return ReflectUtils.getValue(source, name);
  }
}
