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
package vip.justlive.oxygen.core.convert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.NumberUtils;

/**
 * String - Number 解析器
 *
 * @author wubo
 * @see Byte
 * @see Short
 * @see Integer
 * @see Long
 * @see java.math.BigInteger
 * @see Float
 * @see Double
 * @see java.math.BigDecimal
 */
public class StringToNumberConverterFactory implements ConverterFactory<String, Number> {

  @Override
  public <T extends Number> Converter<String, T> getConverter(Class<T> targetType) {
    return new StringToNumber<>(targetType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Converter<Object, Object>> converters() {
    List<Converter<Object, Object>> converters = new ArrayList<>();
    for (Class<? extends Number> clazz : NumberUtils.STANDARD_NUMBER_TYPES) {
      Converter<?, ?> c = new StringToNumber<>(clazz);
      converters.add((Converter<Object, Object>) c);
    }
    return converters;
  }

  private static final class StringToNumber<T extends Number> implements Converter<String, T> {

    private final Class<T> targetType;

    public StringToNumber(Class<T> targetType) {
      this.targetType = targetType;
    }

    @Override
    public T convert(String source) {
      if (source == null || source.isEmpty()) {
        return null;
      }
      return NumberUtils.parseNumber(source, this.targetType);
    }

    @Override
    public Set<ConverterTypePair> pairs() {
      Set<ConverterTypePair> pairs = new HashSet<>(2, 1f);
      pairs.add(ConverterTypePair.create(String.class, targetType));
      Class<?> unwrapClass = ClassUtils.unwrap(targetType);
      if (unwrapClass != targetType) {
        pairs.add(ConverterTypePair.create(String.class, unwrapClass));
      }
      return pairs;
    }
  }
}
