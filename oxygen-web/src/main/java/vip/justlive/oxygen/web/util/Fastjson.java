/*
 * Copyright (C) 2025 the original author or authors.
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

package vip.justlive.oxygen.web.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import vip.justlive.oxygen.core.util.json.Reader;
import vip.justlive.oxygen.core.util.json.Writer;

/**
 * fastjson
 *
 * @author wu-bo
 */
public class Fastjson implements Writer, Reader {

  @Override
  public <T> T parse(String json, Type type) {
    return JSON.parseObject(json, type);
  }

  @Override
  public <T> T bindTo(Object data, Type type) {
    if (data instanceof JSON jsonData) {
      return jsonData.toJavaObject(type);
    } else if (data instanceof Map<?, ?> map) {
      Map<String, Object> values = new HashMap<>(4);
      map.forEach((k, v) -> values.put(k.toString(), v));
      return new JSONObject(values).toJavaObject(type);
    }
    return JSON.parseObject(JSON.toJSONString(data), type);
  }

  @Override
  public String toJson(Object obj) {
    return JSON.toJSONString(obj);
  }
}
