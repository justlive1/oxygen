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


import java.util.HashSet;
import java.util.Set;

/**
 * String - Character转换器
 *
 * @author wubo
 */
public class StringToCharacterConverter implements Converter<String, Character> {

  @Override
  public Character convert(String source) {
    if (source == null || source.isEmpty()) {
      return null;
    }
    if (source.length() > 1) {
      throw new IllegalArgumentException(
          String.format("Can only convert String[%s] to Character", source));
    }
    return source.charAt(0);
  }

  @Override
  public Set<ConverterTypePair> pairs() {
    Set<ConverterTypePair> pairs = new HashSet<>(2, 1f);
    pairs.add(ConverterTypePair.create(String.class, Character.class));
    pairs.add(ConverterTypePair.create(String.class, char.class));
    return pairs;
  }
}
