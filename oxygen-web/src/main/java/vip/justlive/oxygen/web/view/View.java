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
package vip.justlive.oxygen.web.view;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * 视图
 *
 * @author wubo
 */
@Data
public class View {

  /**
   * 视图路径
   */
  private String path;

  /**
   * 是否是重定向
   */
  private boolean redirect;

  /**
   * 数据
   */
  private Map<String, Object> data;

  /**
   * 添加属性
   *
   * @param key key
   * @param value value
   * @return view
   */
  public View addAttribute(String key, Object value) {
    if (data == null) {
      data = new HashMap<>(4);
    }
    data.put(key, value);
    return this;
  }

  /**
   * 添加属性集合
   *
   * @param attrs 属性集合
   * @return view
   */
  public View addAttributes(Map<String, Object> attrs) {
    if (data == null) {
      data = new HashMap<>(attrs);
    } else {
      data.putAll(attrs);
    }
    return this;
  }
}
