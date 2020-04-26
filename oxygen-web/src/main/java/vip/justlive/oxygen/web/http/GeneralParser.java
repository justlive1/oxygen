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
package vip.justlive.oxygen.web.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.net.aio.core.ChannelContext;
import vip.justlive.oxygen.core.util.Bytes;
import vip.justlive.oxygen.core.util.HttpHeaders;
import vip.justlive.oxygen.core.util.IoUtils;
import vip.justlive.oxygen.core.util.Strings;
import vip.justlive.oxygen.core.util.Urls;
import vip.justlive.oxygen.ioc.annotation.Bean;
import vip.justlive.oxygen.web.WebConf;

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
    parseUri(request);
    parsePath(request);
    if (request.getAttribute(Request.ORIGINAL_REQUEST) instanceof ChannelContext) {
      parseQueryString(request);
      return;
    }
    new ServletParser().parse(request);
  }

  private void parseUri(Request request) {
    int index = request.getRequestUri().indexOf(Strings.QUESTION_MARK);
    if (index < 0) {
      request.path = request.getRequestUri();
      return;
    }
    request.path = request.getRequestUri().substring(0, index);
    request.queryString = request.getRequestUri().substring(index + 1);
  }

  private void parsePath(Request request) {
    if (!Strings.hasText(request.path)) {
      return;
    }
    String path = null;
    if (request.path.startsWith(HttpHeaders.HTTP_PREFIX)) {
      path = request.path.substring(HttpHeaders.HTTP_PREFIX.length());
    } else if (request.path.startsWith(HttpHeaders.HTTPS_PREFIX)) {
      path = request.path.substring(HttpHeaders.HTTPS_PREFIX.length());
    }

    if (path != null) {
      int slash = path.indexOf(Strings.SLASH);
      if (slash > -1) {
        request.host = path.substring(0, slash);
        path = path.substring(slash);
      } else {
        request.host = path;
        path = Strings.SLASH;
      }
    } else {
      path = request.path;
    }
    if (path.length() == 0) {
      path = Strings.SLASH;
    }
    request.path = path;
  }

  private void parseQueryString(Request request) {
    if (request.queryString == null) {
      return;
    }
    int index = 0;
    char[] chars = request.queryString.toCharArray();
    String key = null;
    int start = index;
    while (index < chars.length) {
      char c = chars[index];
      if (c == Bytes.OCTOTHORP) {
        break;
      } else if (c == Bytes.EQUAL && key == null) {
        key = Urls.urlDecode(request.queryString.substring(start, index),
            Charset.forName(request.encoding));
        start = index + 1;
      } else if (c == Bytes.AND && key != null) {
        margeParam(request.getParams(), key,
            Urls.urlDecode(request.queryString.substring(start, index),
                Charset.forName(request.encoding)));
        key = null;
        start = index + 1;
      }
      index++;
    }
    if (key != null) {
      margeParam(request.getParams(), key,
          Urls.urlDecode(request.queryString.substring(start, index),
              Charset.forName(request.encoding)));
    }
  }

  private class ServletParser {

    void parse(Request request) {
      HttpServletRequest req = (HttpServletRequest) request.getAttribute(Request.ORIGINAL_REQUEST);
      request.contextPath = req.getContextPath();
      request.secure = req.isSecure();
      request.remoteAddress = req.getRemoteAddr();
      try {
        req.setCharacterEncoding(ConfigFactory.load(WebConf.class).getCharset());
      } catch (UnsupportedEncodingException e) {
        //ignore
      }

      req.getParameterMap().forEach((k, v) -> margeParam(request.getParams(), k, v));

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
          request.body = IoUtils.toBytes(req.getInputStream());
        }
      } catch (IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }
}
