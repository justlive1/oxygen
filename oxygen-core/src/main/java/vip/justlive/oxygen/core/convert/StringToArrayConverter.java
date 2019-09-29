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

import java.lang.reflect.Array;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.Strings;

/**
 * String - 数组 转换器
 *
 * @author wubo
 */
public class StringToArrayConverter implements ArrayConverter {

  private final ConverterService converterService;

  public StringToArrayConverter(ConverterService converterService) {
    this.converterService = converterService;
  }

  @Override
  public boolean support(Class<?> sourceType, Class<?> targetType) {
    return String.class.equals(sourceType) && targetType.isArray();
  }

  @Override
  public Object convert(Object source, Class<?> sourceType, Class<?> targetType) {

    if (support(sourceType, targetType)) {
      if (source == null || source.toString().length() == 0) {
        return null;
      }
      String[] sourceArray = source.toString().split(Strings.COMMA);
      Class<?> componentType = targetType.getComponentType();
      Object targetArray = Array.newInstance(componentType, sourceArray.length);

      for (int i = 0; i < sourceArray.length; i++) {
        Object value = converterService.convert(sourceArray[i], componentType);
        Array.set(targetArray, i, value);
      }
      return targetArray;
    }

    throw Exceptions.fail(String
        .format("unsupported [%s] convert from [%s] to [%s]", source, sourceType, targetType));
  }

  @Override
  public ConverterTypePair pair() {
    return ConverterTypePair.create(String.class, Array.class);
  }

}
