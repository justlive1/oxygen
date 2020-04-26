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


import java.util.HashSet;
import java.util.Set;

/**
 * 类型转换器
 *
 * @param <S> 泛型类
 * @param <T> 泛型类
 * @author wubo
 */
public interface Converter<S, T> {


  /**
   * 类型转换
   *
   * @param source 源
   * @return 转换类
   */
  T convert(S source);

  /**
   * 获取类型对
   *
   * @return 类型对
   */
  default ConverterTypePair pair() {
    return null;
  }

  /**
   * 获取类型对
   *
   * @return 类型对集合
   */
  default Set<ConverterTypePair> pairs() {
    Set<ConverterTypePair> pairs = new HashSet<>(1, 1f);
    if (pair() != null) {
      pairs.add(pair());
    }
    return pairs;
  }
}
