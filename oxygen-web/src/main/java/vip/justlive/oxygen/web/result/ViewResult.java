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
package vip.justlive.oxygen.web.result;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import vip.justlive.oxygen.web.http.Request;

/**
 * view 视图结果
 *
 * @author wubo
 */
@Data
public class ViewResult implements Result {

  /**
   * 视图路径
   */
  private String path;

  /**
   * 数据
   */
  private Map<String, Object> data = new HashMap<>(4);

  public ViewResult() {
    Request request = Request.current();
    addAttribute("ctx", request.getContextPath()).addAttribute("request", request);
  }

  /**
   * 添加属性
   *
   * @param key key
   * @param value value
   * @return view
   */
  public ViewResult addAttribute(String key, Object value) {
    data.put(key, value);
    return this;
  }

  /**
   * 添加属性集合
   *
   * @param attrs 属性集合
   * @return view
   */
  public ViewResult addAttributes(Map<String, Object> attrs) {
    data.putAll(attrs);
    return this;
  }
}
