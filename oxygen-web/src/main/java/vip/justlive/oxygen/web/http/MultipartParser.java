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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.ioc.annotation.Bean;

/**
 * multipart请求解析
 *
 * @author wubo
 */
@Bean
public class MultipartParser implements Parser {

  @Override
  public int order() {
    return 120;
  }

  @Override
  public void parse(Request request) {
    if (!request.isMultipart() || request.body == null) {
      return;
    }
    try {
      MultipartStream multipart = new MultipartStream(new ByteArrayInputStream(request.body),
          request.getMultipart().getBoundary(), request.getEncoding());
      multipart.readMultipartItem();
      multipart.formData.forEach((k, v) -> margeParam(request.getParams(), k, v));
      Map<String, MultipartItem> map = request.getMultipart().getData();
      for (MultipartItem item : multipart.items) {
        map.put(item.getName(), item);
      }
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

}
