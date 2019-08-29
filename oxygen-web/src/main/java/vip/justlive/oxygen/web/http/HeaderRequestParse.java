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

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * header解析器
 *
 * @author wubo
 */
public class HeaderRequestParse implements RequestParse {

  @Override
  public int order() {
    return -2;
  }

  @Override
  public boolean supported(HttpServletRequest req) {
    return req.getHeaderNames() != null;
  }

  @Override
  public void handle(HttpServletRequest req) {
    Enumeration<String> headerNames = req.getHeaderNames();
    Request request = Request.current();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> headers = req.getHeaders(headerName);
      List<String> values = new LinkedList<>();
      while (headers.hasMoreElements()) {
        values.add(headers.nextElement());
      }
      margeParam(request.getHeaders(), headerName, values.toArray(new String[0]));
    }
    parseContentType(request, req);
  }

  private void parseContentType(Request request, HttpServletRequest req) {
    String contentType = request.getHeader(Constants.CONTENT_TYPE);
    if (contentType != null) {
      String[] arr = contentType.split(Constants.SEMICOLON);
      request.contentType = arr[0];
      for (int i = 1; i < arr.length; i++) {
        String[] args = arr[i].split(Constants.EQUAL);
        if (args.length != 2) {
          continue;
        }
        String key = args[0].trim();
        String value = args[1].trim();
        if (key.equalsIgnoreCase(Constants.CHARSET)) {
          request.encoding = value;
        } else if (key.equalsIgnoreCase(Constants.BOUNDARY) && HttpMethod.POST.name()
            .equalsIgnoreCase(req.getMethod()) && contentType.toLowerCase(Locale.ENGLISH)
            .startsWith(Constants.MULTIPART)) {
          request.multipart = new Multipart(value, request.getEncoding());
        }
      }
    }
  }
}
