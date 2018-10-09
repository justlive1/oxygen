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

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * queryString解析器
 * <br>
 * 解析路径后携带的参数与application/x-www-form-urlencoded类型的请求
 *
 * @author wubo
 */
public class QueryRequestParse extends AbstractRequestParse {

  @Override
  public boolean supported(HttpServletRequest req) {
    Map<String, String[]> map = req.getParameterMap();
    return map != null && !map.isEmpty();
  }

  @Override
  public void handle(HttpServletRequest req) {
    Request request = Request.current();
    margeParam(request.getParams(), request.getOriginalRequest().getParameterMap());
  }

}
