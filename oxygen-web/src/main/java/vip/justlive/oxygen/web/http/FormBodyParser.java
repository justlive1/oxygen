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

import java.nio.charset.Charset;
import java.util.Map;
import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.base.Urls;

/**
 * form body 解析器
 *
 * @author wubo
 */
@Bean
public class FormBodyParser implements Parser {

  @Override
  public int order() {
    return 90;
  }

  @Override
  public void parse(Request request) {
    if (request.body == null || request.body.length == 0 || !HttpHeaders.APPLICATION_FORM_URLENCODED
        .equalsIgnoreCase(request.getContentType())) {
      return;
    }

    Map<String, String[]> map = request.getParams();
    Charset charset = Charset.forName(request.encoding);
    for (String line : new String(request.body, Charset.forName(request.getEncoding()))
        .split(Strings.AND)) {
      String[] arr = line.split(Strings.EQUAL);
      if (arr.length == 2) {
        margeParam(map, Urls.urlDecode(arr[0], charset), Urls.urlDecode(arr[1], charset));
      }
    }
  }
}
