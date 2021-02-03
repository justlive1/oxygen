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

package vip.justlive.oxygen.web.http;

import java.util.Map;
import vip.justlive.oxygen.core.Order;

/**
 * 请求解析器
 *
 * @author wubo
 */
public interface Parser extends Order {

  /**
   * 解析
   *
   * @param request 请求
   */
  void parse(Request request);

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
      System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
    }
    map.put(key, newValues);
  }

}
