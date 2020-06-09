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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.net.http.HttpMethod;

/**
 * Router
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class Router {

  private final Map<String, Route> SIMPLE_HANDLERS = new ConcurrentHashMap<>(4, 1);
  private final Map<String, Route> REGEX_HANDLERS = new ConcurrentHashMap<>(4, 1);
  private final Map<String, RouteHandler> STATIC_HANDLERS = new HashMap<>(4, 1);
  private final List<Route> ROUTES = new LinkedList<>();
  private final List<StaticRoute> STATIC_ROUTES = new LinkedList<>();

  /**
   * 构建router
   *
   * @return route
   */
  public Route router() {
    Route route = new Route();
    ROUTES.add(route);
    return route;
  }

  /**
   * 构建静态资源route
   *
   * @return route
   */
  public StaticRoute staticRoute() {
    StaticRoute route = new StaticRoute();
    STATIC_ROUTES.add(route);
    return route;
  }

  /**
   * build
   */
  public void build() {
    ROUTES.forEach(Router::buildRoute);
    STATIC_ROUTES.forEach(Router::buildStaticRoute);
  }

  /**
   * clear
   */
  public void clear() {
    SIMPLE_HANDLERS.clear();
    REGEX_HANDLERS.clear();
  }

  /**
   * lookup route
   *
   * @param method request method
   * @param path request path
   * @return route
   */
  public Route lookup(HttpMethod method, String path) {
    Route route = SIMPLE_HANDLERS.get(path);
    if (route != null && (route = route.match(path, method)) != null) {
      return route;
    }
    for (Route rt : REGEX_HANDLERS.values()) {
      if ((route = rt.match(path, method)) != null) {
        return route;
      }
    }
    return null;
  }

  /**
   * 获取路径支持的方法
   *
   * @param path 路径
   * @return http methods
   */
  public Set<HttpMethod> getAllows(String path) {
    Route route = SIMPLE_HANDLERS.get(path);
    if (route != null) {
      return new HashSet<>(route.methods());
    }
    for (Map.Entry<String, Route> entry : REGEX_HANDLERS.entrySet()) {
      if (Pattern.compile(entry.getKey()).matcher(path).matches()) {
        return new HashSet<>(entry.getValue().methods());
      }
    }
    return new HashSet<>(0);
  }

  /**
   * lookup static handle
   *
   * @param path path
   * @return handle
   */
  public RouteHandler lookupStatic(String path) {
    for (Map.Entry<String, RouteHandler> entry : STATIC_HANDLERS.entrySet()) {
      if (path.startsWith(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  private void buildRoute(Route route) {
    if (route.methods().isEmpty()) {
      for (HttpMethod method : HttpMethod.values()) {
        if (method != HttpMethod.UNKNOWN) {
          route.methods().add(method);
        }
      }
    }
    Route exist;
    if (route.regex()) {
      exist = REGEX_HANDLERS.putIfAbsent(route.path(), route);
    } else {
      exist = SIMPLE_HANDLERS.putIfAbsent(route.path(), route);
    }
    if (exist != null) {
      Set<HttpMethod> temp = new HashSet<>(exist.methods());
      temp.addAll(route.methods());
      if (temp.size() < exist.methods().size() + route.methods().size()) {
        throw Exceptions.fail(String.format("path [%s] already exists", route.path()));
      }
      exist.next(route.next(exist.next()));
    }
    if (log.isDebugEnabled()) {
      log.debug("build route: {}", route);
    }
  }

  private void buildStaticRoute(StaticRoute route) {
    if (STATIC_HANDLERS.putIfAbsent(route.prefix(), new StaticRouteHandler(route)) != null) {
      throw Exceptions.fail(String.format("path [%s] already exists", route.prefix()));
    }
    if (log.isDebugEnabled()) {
      log.debug("build route: {}", route);
    }
  }
}
