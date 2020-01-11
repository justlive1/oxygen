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

import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;

/**
 * impl of RoutingContext
 *
 * @author wubo
 */
public class RoutingContextImpl implements RoutingContext {

  private final Request request;
  private final Response response;

  public RoutingContextImpl(Request request, Response response) {
    this.request = request;
    this.response = response;
  }

  @Override
  public Request request() {
    return request;
  }

  @Override
  public Response response() {
    return response;
  }

  @Override
  public String requestPath() {
    return request.getPath();
  }

  @Override
  public <T> T bindParam(Class<T> clazz) {
    Map<String, Object> map = new HashMap<>(16);
    request.getParams().forEach((k, v) -> map.put(k, v[0]));
    map.putAll(request.getBodyParams());
    return bind(map, clazz);
  }

  @Override
  public <T> T bindCookie(Class<T> clazz) {
    Map<String, Object> map = new HashMap<>(16);
    request.getCookies().forEach((k, v) -> map.put(k, v.getValue()));
    return bind(map, clazz);
  }

  @Override
  public <T> T bindHeader(Class<T> clazz) {
    Map<String, Object> map = new HashMap<>(16);
    request.getHeaders().forEach((k, v) -> map.put(k, v[0]));
    return bind(map, clazz);
  }

  @Override
  public <T> T bindPathVariables(Class<T> clazz) {
    Map<String, Object> map = new HashMap<>(2);
    int len = Request.PATH_VARS.length();
    request.getAttributes().forEach((k, v) -> {
      if (k.startsWith(Request.PATH_VARS)) {
        map.put(k.substring(len), v);
      }
    });
    return bind(map, clazz);
  }

  private <T> T bind(Map<String, Object> map, Class<T> clazz) {
    if (Map.class.isAssignableFrom(clazz)) {
      return clazz.cast(map);
    }
    // 判断非java内置类
    if (!ClassUtils.isJavaInternalType(clazz)) {
      return new JSONObject(map).toJavaObject(clazz);
    }
    return null;
  }
}
