/*
 * Copyright (C) 2019 the original author or authors.
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
import java.nio.charset.Charset;
import java.util.Map;
import vip.justlive.oxygen.core.util.HttpHeaders;
import vip.justlive.oxygen.ioc.annotation.Bean;

/**
 * json请求解析
 *
 * @author wubo
 */
@Bean
public class JsonBodyParser implements Parser {

  @Override
  public int order() {
    return 100;
  }

  @Override
  public void parse(Request request) {
    if (!HttpHeaders.APPLICATION_JSON.equalsIgnoreCase(request.getContentType())) {
      return;
    }

    String body = new String(request.body, Charset.forName(request.getEncoding()));
    JSONObject jsonObject = JSON.parseObject(body);
    Map<String, String[]> map = request.getParams();
    for (String key : jsonObject.keySet()) {
      margeParam(map, key, jsonObject.getString(key));
    }
  }
}
