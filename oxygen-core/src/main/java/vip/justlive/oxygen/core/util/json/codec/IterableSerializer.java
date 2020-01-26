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
import lombok.RequiredArgsConstructor;

/**
 * Iterable 序列化
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class IterableSerializer implements Serializer {

  private final List<Serializer> serializers;

  @Override
  public boolean supported(Class<?> type, Object value) {
    return value != null && Iterable.class.isAssignableFrom(type);
  }

  @Override
  public void serialize(Object value, StringBuilder buf) {
    Iterable<?> iterable = (Iterable<?>) value;
    buf.append('[');
    processIterable(iterable, buf);
    buf.append(']');
  }

  private void processIterable(Iterable<?> iterable, StringBuilder buf) {
    boolean del = false;
    for (Object object : iterable) {
      Serializer serializer = Serializer.lookup(serializers, object);
      if (serializer == null) {
        continue;
      }
      serializer.serialize(object, buf);
      buf.append(',');
      del = true;
    }
    if (del) {
      buf.deleteCharAt(buf.length() - 1);
    }
  }

}
