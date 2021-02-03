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

import com.alibaba.fastjson.JSON;
import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * json result
 *
 * @author wubo
 */
@Bean
public class JsonResultHandler implements ResultHandler {

  @Override
  public boolean support(Result result) {
    return result != null && JsonResult.class.isAssignableFrom(result.getClass());
  }

  @Override
  public void apply(RoutingContext ctx, Result result) {
    JsonResult data = (JsonResult) result;
    Response response = ctx.response();
    response.setContentType(HttpHeaders.APPLICATION_JSON);
    response.write(JSON.toJSONString(data.getData()));
  }
}
