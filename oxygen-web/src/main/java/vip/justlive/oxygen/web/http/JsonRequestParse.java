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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * json请求解析
 *
 * @author wubo
 */
public class JsonRequestParse extends AbstractRequestParse {

  @Override
  public boolean supported(HttpServletRequest req) {
    return Constants.APPLICATION_JSON.equalsIgnoreCase(Request.current().getContentType());
  }

  @Override
  public void handle(HttpServletRequest req) {
    JSONObject jsonObject = JSON.parseObject(readBody(req));
    Map<String, String[]> map = Request.current().getParams();
    for (String key : jsonObject.keySet()) {
      margeParam(map, key, jsonObject.getString(key));
    }
  }
}
