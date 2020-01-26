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
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * map 序列化
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class MapSerializer implements Serializer {

  private final List<Serializer> serializers;

  @Override
  public boolean supported(Class<?> type, Object value) {
    return value != null && Map.class.isAssignableFrom(type);
  }

  @Override
  public void serialize(Object value, StringBuilder buf) {
    buf.append('{');
    boolean del = false;
    for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
      Serializer serializer = Serializer.lookup(serializers, entry.getValue());
      if (serializer == null) {
        continue;
      }
      buf.append('"').append(getKey(entry)).append("\":");
      serializer.serialize(entry.getValue(), buf);
      buf.append(',');
      del = true;
    }
    if (del) {
      buf.deleteCharAt(buf.length() - 1).append('}');
    }
  }

  private String getKey(Map.Entry<?, ?> entry) {
    Object key = entry.getKey();
    if (key == null) {
      return "null";
    }
    return key.toString();
  }
}
