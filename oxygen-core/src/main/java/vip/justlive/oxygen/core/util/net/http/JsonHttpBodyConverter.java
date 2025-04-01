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
import java.lang.reflect.Type;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.json.Json;

/**
 * application/json http body 转换期
 *
 * @author wubo
 * @since 3.0.10
 */
public class JsonHttpBodyConverter implements HttpBodyConverter {

  @Override
  public boolean canWrite(HttpRequest request) {
    if (request.getHttpBody() == HttpBody.JSON) {
      return true;
    }
    String contentType = request.getHeaders().get(HttpHeaders.CONTENT_TYPE);
    return contentType != null && contentType.startsWith(HttpHeaders.APPLICATION_JSON);

  }

  @Override
  public void write(HttpRequest request, OutputStream out) throws IOException {
    if (request.getBody() == null) {
      return;
    }
    String res;
    if (request.getBody() instanceof String) {
      res = (String) request.getBody();
    } else {
      res = Json.toJson(request.getBody());
    }
    out.write(res.getBytes(request.getCharset()));
  }

  @Override
  public boolean canRead(HttpResponse response, Type type) {
    String contentType = HttpHeaders.getSimpleHeader(HttpHeaders.CONTENT_TYPE,
        response.getHeaders());
    if (Strings.hasText(contentType) && !contentType.startsWith(HttpHeaders.APPLICATION_JSON)) {
      return false;
    }
    return Json.getReader() != null;
  }

  @Override
  public <T> T read(HttpResponse response, Type type) {
    try {
      return Json.parse(response.bodyAsString(), type);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
