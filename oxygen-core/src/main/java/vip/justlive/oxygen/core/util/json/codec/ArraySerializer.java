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

import java.lang.reflect.Array;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * array序列化
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class ArraySerializer implements Serializer {

  private final List<Serializer> serializers;

  @Override
  public int order() {
    return 50;
  }

  @Override
  public boolean supported(Class<?> type, Object value) {
    return value != null && type.isArray();
  }

  @Override
  public void serialize(Object value, StringBuilder buf) {
    buf.append('[');
    int length = Array.getLength(value);
    boolean del = false;
    for (int i = 0; i < length; i++) {
      Object item = Array.get(value, i);
      Serializer serializer = Serializer.lookup(serializers, item);
      if (serializer == null) {
        continue;
      }
      serializer.serialize(item, buf);
      buf.append(',');
      del = true;
    }
    if (del) {
      buf.deleteCharAt(buf.length() - 1);
    }
    buf.append(']');
  }

}
