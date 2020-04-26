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
package vip.justlive.oxygen.core.convert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * String - Boolean 转换器
 * <br>
 * 注意: null和空字符串被解析为false
 *
 * @author wubo
 */
public class StringToBooleanConverter implements Converter<String, Boolean> {

  /**
   * 表示true的字符值
   */
  private static final List<String> TRUE_VALUES = Arrays.asList("true", "on", "yes", "1");

  /**
   * 表示false的字符值
   */
  private static final List<String> FALSE_VALUES = Arrays.asList("false", "off", "no", "0");


  @Override
  public Boolean convert(String source) {

    String value = source;
    if (value == null || value.trim().isEmpty()) {
      return Boolean.FALSE;
    }

    value = value.toLowerCase();

    if (TRUE_VALUES.contains(value)) {
      return Boolean.TRUE;
    } else if (FALSE_VALUES.contains(value)) {
      return Boolean.FALSE;
    } else {
      throw Exceptions.fail("Invalid boolean value '" + source + "'");
    }
  }

  @Override
  public Set<ConverterTypePair> pairs() {
    Set<ConverterTypePair> pairs = new HashSet<>(2, 1f);
    pairs.add(ConverterTypePair.create(String.class, boolean.class));
    pairs.add(ConverterTypePair.create(String.class, Boolean.class));
    return pairs;
  }
}
