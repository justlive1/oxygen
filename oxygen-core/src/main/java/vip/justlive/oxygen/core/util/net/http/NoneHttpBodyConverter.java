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

import java.io.OutputStream;

/**
 * NONE http body 转换期
 *
 * @author wubo
 * @since 3.0.10
 */
public class NoneHttpBodyConverter implements HttpBodyConverter {

  @Override
  public int order() {
    return -1;
  }

  @Override
  public boolean canWrite(HttpRequest request) {
    return request.getHttpBody() == HttpBody.NONE;
  }

  @Override
  public void write(HttpRequest request, OutputStream out) {
    // nothing
  }
}
