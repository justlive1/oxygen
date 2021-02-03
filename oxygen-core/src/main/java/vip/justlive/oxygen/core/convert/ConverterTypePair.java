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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import vip.justlive.oxygen.core.util.base.MoreObjects;

/**
 * 转换类型对
 *
 * @author wubo
 */
public class ConverterTypePair {

  private static final Map<Integer, ConverterTypePair> CACHE = new ConcurrentHashMap<>(8);

  /**
   * 源类型
   */
  private final Class<?> sourceType;
  /**
   * 目标类型
   */
  private final Class<?> targetType;

  private ConverterTypePair(Class<?> sourceType, Class<?> targetType) {
    this.sourceType = sourceType;
    this.targetType = targetType;
  }

  public static ConverterTypePair create(Class<?> sourceType, Class<?> targetType) {
    MoreObjects.notNull(sourceType, "Source type must not be null");
    MoreObjects.notNull(targetType, "Target type must not be null");
    Integer key = hash(sourceType, targetType);
    ConverterTypePair pair = CACHE.get(key);
    if (pair == null) {
      pair = new ConverterTypePair(sourceType, targetType);
      ConverterTypePair out = CACHE.putIfAbsent(key, pair);
      if (out != null) {
        return out;
      }
    }
    return pair;
  }

  private static int hash(Class<?> sourceType, Class<?> targetType) {
    return (sourceType.hashCode() * 31 + targetType.hashCode());
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || other.getClass() != ConverterTypePair.class) {
      return false;
    }
    ConverterTypePair otherPair = (ConverterTypePair) other;
    return (this.sourceType == otherPair.sourceType && this.targetType == otherPair.targetType);
  }

  @Override
  public int hashCode() {
    return hash(this.sourceType, this.targetType);
  }

  @Override
  public String toString() {
    return (this.sourceType.getName() + " -> " + this.targetType.getName());
  }

}
