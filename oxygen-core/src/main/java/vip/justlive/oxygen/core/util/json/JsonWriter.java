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

package vip.justlive.oxygen.core.util.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.util.base.ServiceLoaderUtils;
import vip.justlive.oxygen.core.util.json.codec.ArraySerializer;
import vip.justlive.oxygen.core.util.json.codec.AtomicArraySerializer;
import vip.justlive.oxygen.core.util.json.codec.BeanSerializer;
import vip.justlive.oxygen.core.util.json.codec.IterableSerializer;
import vip.justlive.oxygen.core.util.json.codec.MapSerializer;
import vip.justlive.oxygen.core.util.json.codec.ReferenceSerializer;
import vip.justlive.oxygen.core.util.json.codec.Serializer;

/**
 * json writer
 *
 * @author wubo
 */
@Slf4j
public class JsonWriter {

  @Getter
  private final List<Serializer> serializers = new ArrayList<>();

  public JsonWriter() {
    serializers.addAll(ServiceLoaderUtils.loadServices(Serializer.class));
    serializers.add(new BeanSerializer(serializers));
    serializers.add(new MapSerializer(serializers));
    serializers.add(new IterableSerializer(serializers));
    serializers.add(new ArraySerializer(serializers));
    serializers.add(new AtomicArraySerializer(serializers));
    serializers.add(new ReferenceSerializer(serializers));
    Collections.sort(serializers);
  }

  /**
   * 添加序列化
   *
   * @param serializer 序列化
   */
  public void addSerializer(Serializer serializer) {
    if (serializers.contains(serializer)) {
      return;
    }
    serializers.add(serializer);
    Collections.sort(serializers);
  }

  /**
   * to json
   *
   * @param value 对象
   * @return json
   */
  public String toJson(Object value) {
    StringBuilder buf = new StringBuilder();
    Serializer serializer = Serializer.lookup(serializers, value);
    if (serializer != null) {
      serializer.serialize(value, buf);
      return buf.toString();
    }
    log.warn("value {} can not serialize to json", value);
    return buf.toString();
  }

}
