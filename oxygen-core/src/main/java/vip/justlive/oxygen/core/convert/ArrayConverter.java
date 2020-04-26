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
 * 数组类型转换器
 *
 * @author wubo
 */
public interface ArrayConverter {

  /**
   * 是否支持转换
   *
   * @param sourceType 源类型
   * @param targetType 目标类型
   * @return true为支持
   */
  boolean support(Class<?> sourceType, Class<?> targetType);

  /**
   * 转换
   *
   * @param source 源数据
   * @param sourceType 源数据类型
   * @param targetType 目标类型
   * @return 转换后数据
   */
  Object convert(Object source, Class<?> sourceType, Class<?> targetType);

  /**
   * 获取类型对
   *
   * @return 类型对
   */
  default ConverterTypePair pair() {
    return null;
  }
}
