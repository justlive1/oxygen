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

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.net.aio.core.ChannelContext;
import vip.justlive.oxygen.core.util.IOUtils;
import vip.justlive.oxygen.ioc.annotation.Bean;

/**
 * 请求基础解析
 *
 * @author wubo
 */
@Bean
public class GeneralParser implements Parser {

  @Override
  public int order() {
    return -1;
  }

  @Override
  public void parse(Request request) {
    if (request.getAttribute(Request.ORIGINAL_REQUEST) instanceof ChannelContext) {
      return;
    }
    new ServletParser().parse(request);
  }

  private class ServletParser {

    void parse(Request request) {
      HttpServletRequest req = (HttpServletRequest) request.getAttribute(Request.ORIGINAL_REQUEST);
      request.contextPath = req.getContextPath();
      request.secure = req.isSecure();
      request.remoteAddress = req.getRemoteAddr();

      Enumeration<String> headerNames = req.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        Enumeration<String> headers = req.getHeaders(headerName);
        List<String> values = new LinkedList<>();
        while (headers.hasMoreElements()) {
          values.add(headers.nextElement());
        }
        margeParam(request.getHeaders(), headerName, values.toArray(new String[0]));
      }

      try {
        ServletInputStream in = req.getInputStream();
        if (in != null) {
          request.body = IOUtils.toBytes(req.getInputStream());
        }
      } catch (IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }
}
