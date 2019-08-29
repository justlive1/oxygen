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

import java.util.List;

/**
 * 转换工厂，用于将S转换成R的子类
 *
 * @param <S> 泛型类
 * @param <R> 泛型类
 * @author wubo
 */
public interface ConverterFactory<S, R> {

  /**
   * 获取转换器
   *
   * @param targetType 目标类型
   * @param <T> 泛型类
   * @return 转换器
   */
  <T extends R> Converter<S, T> getConverter(Class<T> targetType);

  /**
   * 获取所有支持的转换器
   *
   * @return 转换器列表
   */
  List<Converter<Object, Object>> converters();
}
