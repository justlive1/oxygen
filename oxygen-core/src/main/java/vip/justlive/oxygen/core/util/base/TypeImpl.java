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
package vip.justlive.oxygen.core.util.base;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;

/**
 * type impl
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class TypeImpl implements Type {

  private final Type type;

  @Override
  public String getTypeName() {
    if (type instanceof Class<?>) {
      return ClassUtils.getClassName((Class<?>) type);
    } else if (type instanceof ParameterizedType) {
      return new ParameterizedTypeImpl((ParameterizedType) type).getTypeName();
    }
    return type.getTypeName();
  }
}
