/*
 * Copyright (C) 2024 the original author or authors.
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

package vip.justlive.oxygen.core.util.net.http;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HttpBodyConverters
 *
 * @author wubo
 * @since 3.0.10
 */
public class HttpBodyConverters {

  private static final List<HttpBodyConverter> CONVERTERS = new ArrayList<>();

  static {
    register(new NoneHttpBodyConverter());
    register(new FormHttpBodyConverter());
    register(new MultipartHttpBodyConverter());
    register(new JsonHttpBodyConverter());
  }

  /**
   * 注册
   *
   * @param converter 转换器
   */
  public static void register(HttpBodyConverter converter) {
    if (converter == null) {
      return;
    }
    CONVERTERS.add(converter);
    Collections.sort(CONVERTERS);
  }

  /**
   * 注销
   *
   * @param converter 转换器
   */
  public static void unregister(HttpBodyConverter converter) {
    CONVERTERS.remove(converter);
  }

  public static HttpBodyConverter findCanWrite(HttpRequest request) {
    for (HttpBodyConverter converter : CONVERTERS) {
      if (converter.canWrite(request)) {
        return converter;
      }
    }
    return null;
  }

  public static HttpBodyConverter findCanRead(HttpResponse response, Type type) {
    for (HttpBodyConverter converter : CONVERTERS) {
      if (converter.canRead(response, type)) {
        return converter;
      }
    }
    return null;
  }
}
