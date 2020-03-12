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
package vip.justlive.oxygen.web.result;

import vip.justlive.oxygen.core.util.HttpHeaders;
import vip.justlive.oxygen.core.util.Strings;
import vip.justlive.oxygen.ioc.annotation.Bean;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * Redirect结果处理
 *
 * @author wubo
 */
@Bean
public class RedirectResultHandler implements ResultHandler {

  private static final String PROTOCOLS_REGEX = "^\\w+://.*";
  private static final int DEFAULT_HTTP_PORT = 80;
  private static final int DEFAULT_HTTPS_PORT = 443;

  @Override
  public boolean support(Result result) {
    return result != null && RedirectResult.class.isAssignableFrom(result.getClass());
  }

  @Override
  public void apply(RoutingContext ctx, Result result) {
    RedirectResult data = (RedirectResult) result;
    Request request = ctx.request();
    Response response = ctx.response();
    response.setStatus(data.getStatus());

    StringBuilder sb = new StringBuilder();
    if (!data.getUrl().matches(PROTOCOLS_REGEX)) {
      if (request.isSecure()) {
        sb.append(HttpHeaders.HTTPS_PREFIX);
      } else {
        sb.append(HttpHeaders.HTTP_PREFIX);
      }
      sb.append(request.getHost());
      if (!data.getUrl().startsWith(Strings.SLASH)) {
        sb.append(ctx.requestPath()).append(Strings.SLASH);
      }
    }
    sb.append(data.getUrl());
    response.setHeader(HttpHeaders.LOCATION, sb.toString());
  }
}
