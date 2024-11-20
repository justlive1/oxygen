/*
 * Copyright (C) 2023 the original author or authors.
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

import lombok.Data;
import lombok.experimental.Accessors;
import vip.justlive.oxygen.core.CoreConfigKeys;

/**
 * http request holder
 *
 * @author wubo
 */
@Data
@Accessors(chain = true)
public class HttpRequestHolder {

  private static final ThreadLocal<HttpRequestHolder> LOCAL = new ThreadLocal<>();

  private long requestTime;
  private HttpRequest request;

  private long responseTime;
  private HttpResponse response;

  public static boolean isEnabled() {
    return CoreConfigKeys.HTTP_REQUEST_HOLDER_ENABLED.castValue(boolean.class);
  }

  public static HttpRequestHolder get() {
    return LOCAL.get();
  }

  public static void remove() {
    LOCAL.remove();
  }

  static void set(HttpRequestHolder holder) {
    LOCAL.set(holder);
  }


}
