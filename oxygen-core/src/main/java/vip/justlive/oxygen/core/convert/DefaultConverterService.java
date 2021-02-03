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

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.MoreObjects;

/**
 * 默认转换服务实现类
 *
 * @author wubo
 */
public class DefaultConverterService implements ConverterService, ConverterRegistry {

  private static DefaultConverterService sharedConverterService;

  /**
   * 转换器集合
   */
  private final Map<ConverterTypePair, Converter<Object, Object>> converters = new HashMap<>(8, 1);
  private final Map<ConverterTypePair, ArrayConverter> arrayConverters = new HashMap<>(8, 1);

  public DefaultConverterService() {
    addConverter(new StringToBooleanConverter()).addArrayConverter(new StringToArrayConverter(this))
        .addConverter(new StringToCharacterConverter())
        .addConverterFactory(new StringToNumberConverterFactory());
  }

  /**
   * 获取共享单例类型转换器
   *
   * @return 类型转换器
   */
  public static DefaultConverterService sharedConverterService() {
    DefaultConverterService cs = sharedConverterService;
    if (cs == null) {
      synchronized (DefaultConverterService.class) {
        cs = sharedConverterService;
        if (cs == null) {
          sharedConverterService = new DefaultConverterService();
        }
      }
    }
    return sharedConverterService;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ConverterRegistry addConverter(Converter<?, ?> converter) {
    MoreObjects.notNull(converter);
    for (ConverterTypePair pair : converter.pairs()) {
      converters.put(pair, (Converter<Object, Object>) converter);
    }
    return this;
  }

  @Override
  public ConverterRegistry addConverterFactory(ConverterFactory<?, ?> factory) {
    MoreObjects.notNull(factory).converters().forEach(this::addConverter);
    return this;
  }

  @Override
  public ConverterRegistry addArrayConverter(ArrayConverter converter) {
    MoreObjects.notNull(converter);
    arrayConverters.put(converter.pair(), converter);
    return this;
  }

  @Override
  public boolean canConverter(Class<?> source, Class<?> target) {
    return source.equals(target) || converters
        .containsKey(ConverterTypePair.create(source, target));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T convert(Object source, Class<T> targetType) {
    if (source == null) {
      return null;
    }
    if (source.getClass().equals(targetType)) {
      return (T) source;
    }
    Converter<Object, Object> converter = converters
        .get(ConverterTypePair.create(source.getClass(), targetType));
    if (converter != null) {
      return (T) converter.convert(source);
    }
    if (targetType.isArray()) {
      ArrayConverter arrayConverter = arrayConverters
          .get(ConverterTypePair.create(source.getClass(), Array.class));
      if (arrayConverter != null) {
        return (T) arrayConverter.convert(source, source.getClass(), targetType);
      }
    }
    throw Exceptions.fail(String.format("unsupported convert [%s] to [%s]", source, targetType));
  }
}
