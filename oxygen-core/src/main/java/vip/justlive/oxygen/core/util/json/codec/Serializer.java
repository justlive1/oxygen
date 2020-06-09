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

package vip.justlive.oxygen.core.util.json.codec;

import java.util.List;
import vip.justlive.oxygen.core.Order;

/**
 * 序列化
 *
 * @author wubo
 */
public interface Serializer extends Order {

  /**
   * 寻找可用的序列化
   *
   * @param serializers 序列化集合
   * @param value 要序列化的值
   * @return Serializer
   */
  static Serializer lookup(List<Serializer> serializers, Object value) {
    Class<?> type = null;
    if (value != null) {
      type = value.getClass();
    }
    for (Serializer serializer : serializers) {
      if (serializer.supported(type, value)) {
        return serializer;
      }
    }
    return null;
  }

  /**
   * 是否支持
   *
   * @param type 类型
   * @param value 值
   * @return true为支持
   */
  boolean supported(Class<?> type, Object value);

  /**
   * 序列化
   *
   * @param value 值
   * @param buf buffer
   */
  void serialize(Object value, StringBuilder buf);
}
