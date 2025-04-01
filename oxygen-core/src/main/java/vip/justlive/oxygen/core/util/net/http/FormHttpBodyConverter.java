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

import java.io.IOException;
import java.io.OutputStream;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.MoreObjects;

/**
 * form http body 转换期
 *
 * @author wubo
 * @since 3.0.10
 */
public class FormHttpBodyConverter implements HttpBodyConverter {

  @Override
  public boolean canWrite(HttpRequest request) {
    if (request.getHttpBody() == HttpBody.FORM) {
      return true;
    }
    String contentType = request.getHeaders().get(HttpHeaders.CONTENT_TYPE);
    return contentType != null && contentType.startsWith(HttpHeaders.APPLICATION_FORM_URLENCODED);
  }

  @Override
  public void write(HttpRequest request, OutputStream out) throws IOException {
    if (request.getBody() == null) {
      return;
    }
    String source;
    if (request.getBody() instanceof String) {
      source = (String) request.getBody();
    } else {
      source = MoreObjects.beanToQueryString(request.getBody(), true);
    }
    out.write(source.getBytes(request.getCharset()));
  }
}
