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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import vip.justlive.oxygen.web.router.Route;

/**
 * path请求解析
 *
 * @author wubo
 */
public class PathRequestParse implements RequestParse {

  @Override
  public boolean supported(HttpServletRequest req) {
    Route route = Request.current().getRoute();
    return route != null && !route.pathVars().isEmpty();
  }

  @Override
  public void handle(HttpServletRequest req) {
    Request request = Request.current();
    String path = request.getPath();
    Matcher matcher = Pattern.compile(request.getRoute().path()).matcher(path);
    if (matcher.matches()) {
      Map<String, String> map = request.getPathVariables();
      List<String> pathVariables = request.getRoute().pathVars();
      for (int i = 1, len = matcher.groupCount(); i <= len; i++) {
        map.put(pathVariables.get(i - 1), matcher.group(i));
      }
    }
  }
}
