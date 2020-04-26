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

/**
 * 转换器注册
 *
 * @author wubo
 */
public interface ConverterRegistry {

  /**
   * 注册转换器
   *
   * @param converter 转换器
   * @return 返回当前对象
   */
  ConverterRegistry addConverter(Converter<?, ?> converter);

  /**
   * 注册转换器工厂
   *
   * @param factory 转换器工厂
   * @return 返回当前对象
   */
  ConverterRegistry addConverterFactory(ConverterFactory<?, ?> factory);

  /**
   * 注册数组类型转换器
   *
   * @param converter 转换器
   * @return 返回当前对象
   */
  ConverterRegistry addArrayConverter(ArrayConverter converter);
}
