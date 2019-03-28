/*
 *  Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */
package vip.justlive.oxygen.web.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import vip.justlive.oxygen.core.Order;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * http解析
 *
 * @author wubo
 */
public interface RequestParse extends Order {

  /**
   * 是否支持
   *
   * @param req request
   * @return true表示支持
   */
  boolean supported(HttpServletRequest req);

  /**
   * 解析参数
   *
   * @param req request
   */
  void handle(HttpServletRequest req);

  /**
   * 合并参数
   *
   * @param map 参数
   * @param key 键
   * @param value 值
   */
  default void margeParam(Map<String, String[]> map, String key, String value) {
    margeParam(map, key, new String[]{value});
  }

  /**
   * 合并参数
   *
   * @param map 参数
   * @param key 键
   * @param values 值
   */
  default void margeParam(Map<String, String[]> map, String key, String[] values) {
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

  /**
   * 合并参数
   *
   * @param map 参数
   * @param values 值
   */
  default void margeParam(Map<String, String[]> map, Map<String, String[]> values) {
    values.forEach((k, v) -> margeParam(map, k, v));
  }

  /**
   * 读取body
   *
   * @param req 请求
   * @return body
   */
  default String readBody(HttpServletRequest req) {
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
