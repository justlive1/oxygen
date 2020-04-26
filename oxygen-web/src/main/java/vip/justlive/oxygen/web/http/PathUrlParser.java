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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.net.http.HttpMethod;
import vip.justlive.oxygen.ioc.annotation.Bean;
import vip.justlive.oxygen.web.WebConf;
import vip.justlive.oxygen.web.router.OptionsRouteHandler;
import vip.justlive.oxygen.web.router.Route;
import vip.justlive.oxygen.web.router.RouteHandler;
import vip.justlive.oxygen.web.router.Router;

/**
 * path请求解析
 *
 * @author wubo
 */
@Bean
public class PathUrlParser implements Parser {

  @Override
  public int order() {
    return 80;
  }

  @Override
  public void parse(Request request) {
    RouteHandler handler;
    if (request.method == HttpMethod.OPTIONS && !ConfigFactory.load(WebConf.class)
        .isHandleOptionsRequest()) {
      handler = OptionsRouteHandler.INSTANCE;
    } else {
      handler = Router.lookupStatic(request.getPath());
    }
    Route route = null;
    if (handler != null) {
      request.routeHandler = handler;
    } else {
      route = Router.lookup(request.getMethod(), request.getPath());
      if (route != null) {
        request.routeHandler = route.handler();
      }
    }
    if (route == null || route.pathVars().isEmpty()) {
      return;
    }
    String path = request.getPath();
    Matcher matcher = Pattern.compile(route.path()).matcher(path);
    if (matcher.matches()) {
      List<String> pathVariables = route.pathVars();
      for (int i = 1, len = matcher.groupCount(); i <= len; i++) {
        request.addAttribute(Request.PATH_VARS + pathVariables.get(i - 1), matcher.group(i));
      }
    }
  }
}
