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
import java.util.Objects;
import lombok.RequiredArgsConstructor;

/**
 * ParameterizedType
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class ParameterizedTypeImpl implements ParameterizedType {

  private final ParameterizedType type;

  @Override
  public Type[] getActualTypeArguments() {
    return type.getActualTypeArguments();
  }

  @Override
  public Type getRawType() {
    return type.getRawType();
  }

  @Override
  public Type getOwnerType() {
    return type.getOwnerType();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ParameterizedTypeImpl that = (ParameterizedTypeImpl) o;
    return Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    Type ownerType = getOwnerType();
    Type rawType = new TypeImpl(getRawType());
    if (ownerType != null) {
      sb.append(new TypeImpl(ownerType).getTypeName());
      sb.append(".");
      if (ownerType instanceof ParameterizedType) {
        sb.append(rawType.getTypeName()
            .replace(new TypeImpl(((ParameterizedType) ownerType).getRawType()).getTypeName() + "$",
                ""));
      } else {
        sb.append(rawType.getTypeName());
      }
    } else {
      sb.append(rawType.getTypeName());
    }
    Type[] actualTypeArguments = getActualTypeArguments();
    if (actualTypeArguments != null && actualTypeArguments.length > 0) {
      sb.append("<");
      for (int i = 0; i < actualTypeArguments.length; ++i) {
        sb.append(new TypeImpl(actualTypeArguments[i]).getTypeName());
        if (i < actualTypeArguments.length - 1) {
          sb.append(", ");
        }
      }
      sb.append(">");
    }
    return sb.toString();
  }
}
