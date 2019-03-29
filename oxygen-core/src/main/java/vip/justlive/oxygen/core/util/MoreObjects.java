/*
 * Copyright (C) 2019 justlive1
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

import lombok.experimental.UtilityClass;

/**
 * objects工具增强类
 *
 * @author wubo
 */
@UtilityClass
public class MoreObjects {

  /**
   * 获取第一个不为null的值，没有则返回null
   *
   * @param first first value
   * @param second second value
   * @param <T> 泛型
   * @return nonNull
   */
  public static <T> T firstOrNull(T first, T second) {
    if (first != null) {
      return first;
    }
    return second;
  }

  /**
   * 获取第一个不为null的值
   *
   * @param first first value
   * @param second second value
   * @param <T> 泛型
   * @return nonNull
   */
  public static <T> T firstNonNull(T first, T second) {
    T obj = firstOrNull(first, second);
    if (obj != null) {
      return obj;
    }
    throw new IllegalArgumentException();
  }

  /**
   * 获取第一个不为null或空字符串的值
   *
   * @param first first value
   * @param second second value
   * @param others other values
   * @return nonEmpty
   */
  public static String firstNonEmpty(String first, String second, String... others) {
    if (first != null && first.length() > 0) {
      return first;
    }
    if (second != null && second.length() > 0) {
      return second;
    }
    if (others == null || others.length == 0) {
      throw new IllegalArgumentException();
    }
    for (String str : others) {
      if (str != null && str.length() > 0) {
        return str;
      }
    }
    throw new IllegalArgumentException();
  }
}
