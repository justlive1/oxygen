/*
 * Copyright (C) 2018 justlive1
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
package vip.justlive.oxygen.web.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * request解析抽象类
 *
 * @author wubo
 */
abstract class AbstractRequestParse implements RequestParse {

  void margeParam(Map<String, String[]> map, String key, String value) {
    margeParam(map, key, new String[]{value});
  }

  void margeParam(Map<String, String[]> map, String key, String[] values) {
    String[] oldValues = map.get(key);
    String[] newValues;
    if (oldValues == null) {
      newValues = values;
    } else {
      newValues = new String[oldValues.length + values.length];
      System.arraycopy(oldValues, 0, newValues, 0, newValues.length);
    }
    map.put(key, newValues);
  }

  void margeParam(Map<String, String[]> map, Map<String, String[]> values) {
    values.forEach((k, v) -> margeParam(map, k, v));
  }

  String readBody(HttpServletRequest req) {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = req.getInputStream()) {
      int b;
      while ((b = is.read()) != -1) {
        os.write(b);
      }
      Request request = Request.current();
      return new String(os.toByteArray(), request.getEncoding());
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
