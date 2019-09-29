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
package vip.justlive.oxygen.web.router;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import lombok.Data;
import lombok.experimental.Accessors;
import vip.justlive.oxygen.core.net.http.HttpMethod;

/**
 * 路由
 *
 * @author wubo
 */
@Data
@Accessors(fluent = true)
public class Route {

  public static final String REGEX_PATH_VAR = "\\{\\w+}";
  public static final String REGEX_PATH_VAR_REPLACE = "(\\\\w+)";

  /**
   * 方法
   */
  private Set<HttpMethod> methods = new HashSet<>(1, 1f);
  /**
   * 请求路径
   */
  private String path;
  /**
   * 是否为正则请求
   */
  private boolean regex;
  /**
   * 路由处理方式
   */
  private RouteHandler handler;

  private List<String> pathVars = new LinkedList<>();

  Route() {
  }

  /**
   * 设置method
   *
   * @param httpMethod 请求方法
   * @return router
   */
  public Route method(HttpMethod httpMethod) {
    methods.add(httpMethod);
    return this;
  }

  /**
   * add path
   *
   * @param path request path
   * @return router
   */
  public Route path(String path) {
    Matcher matcher = Router.REGEX_PATH_GROUP.matcher(path);
    int start = 0;
    if (matcher.find(start)) {
      do {
        pathVars.add(matcher.group(1));
        start = matcher.end();
      } while (matcher.find(start));
      path = path.replaceAll(REGEX_PATH_VAR, REGEX_PATH_VAR_REPLACE);
      this.regex = true;
    }
    this.path = path;
    return this;
  }

  /**
   * 添加处理器
   *
   * @param handler 处理
   * @return router
   */
  public Route handler(RouteHandler handler) {
    this.handler = handler;
    return this;
  }

}
