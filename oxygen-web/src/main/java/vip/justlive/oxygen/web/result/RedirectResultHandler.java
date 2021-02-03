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

import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.web.WebConfigKeys;
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
      String contextPath = WebConfigKeys.SERVER_CONTEXT_PATH.getValue();
      if (Strings.hasText(contextPath) && !Strings.SLASH.equals(contextPath)) {
        sb.append(contextPath);
      }
      if (!data.getUrl().startsWith(Strings.SLASH)) {
        sb.append(ctx.requestPath()).append(Strings.SLASH);
      }
    }
    sb.append(data.getUrl());
    response.setHeader(HttpHeaders.LOCATION, sb.toString());
  }
}
