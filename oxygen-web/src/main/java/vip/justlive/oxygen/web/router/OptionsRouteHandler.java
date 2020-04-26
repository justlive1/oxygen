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

package vip.justlive.oxygen.web.router;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import vip.justlive.oxygen.core.net.http.HttpMethod;
import vip.justlive.oxygen.core.util.HttpHeaders;
import vip.justlive.oxygen.core.util.Strings;

/**
 * @author wubo
 */
public class OptionsRouteHandler implements RouteHandler {

  private static final String ALLOW = getAllow(Arrays.asList(HttpMethod.values()));
  public static final OptionsRouteHandler INSTANCE = new OptionsRouteHandler();

  @Override
  public void handle(RoutingContext ctx) {
    Set<HttpMethod> methods = Router.getAllows(ctx.requestPath());
    String allow;
    if (methods.isEmpty()) {
      allow = ALLOW;
    } else {
      methods.add(HttpMethod.OPTIONS);
      allow = getAllow(methods);
    }
    ctx.response().setHeader(HttpHeaders.ALLOW, allow);
  }

  private static String getAllow(Collection<HttpMethod> methods) {
    StringBuilder sb = new StringBuilder();
    for (HttpMethod method : methods) {
      if (method != HttpMethod.UNKNOWN) {
        sb.append(Strings.COMMA).append(method.name());
      }
    }
    sb.deleteCharAt(0);
    return sb.toString();
  }
}
