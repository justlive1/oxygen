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

import java.util.Locale;
import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.oxygen.core.util.net.http.HttpMethod;

/**
 * header解析器
 *
 * @author wubo
 */
@Bean
public class HeaderParser implements Parser {

  @Override
  public void parse(Request request) {
    request.remoteAddress = Strings.firstOrNull(parseRemoteAddress(request), request.remoteAddress);
    parseHost(request);
    parseContentType(request);
    if (request.contextPath != null && !Strings.SLASH.equals(request.contextPath) && request.path
        .startsWith(request.contextPath)) {
      request.path = request.path.substring(request.contextPath.length());
    }
    parseCookie(request);
  }

  private String parseRemoteAddress(Request request) {
    String ip = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
    if (checkIp(ip)) {
      // 应对x-forwarded-for 中返回多个服务器ip的情况
      if (ip.contains(Strings.COMMA)) {
        for (String a : ip.split(Strings.COMMA)) {
          if (checkIp(ip)) {
            return a.trim();
          }
        }
      }
      return ip;
    }
    ip = request.getHeader(HttpHeaders.PROXY_CLIENT_IP);
    if (checkIp(ip)) {
      return ip;
    }
    ip = request.getHeader(HttpHeaders.WL_PROXY_CLIENT_IP);
    if (checkIp(ip)) {
      return ip;
    }
    ip = request.getHeader(HttpHeaders.X_REAL_IP);
    if (checkIp(ip)) {
      return ip;
    }
    Object aioCtx = request.getAttribute(Request.ORIGINAL_REQUEST);
    if (aioCtx instanceof ChannelContext) {
      return ((ChannelContext) aioCtx).getAddress().getAddress().getHostAddress();
    }
    return ip;
  }

  private boolean checkIp(String ip) {
    return ip != null && ip.length() > 0 && !Strings.UNKNOWN.equalsIgnoreCase(ip);
  }

  private void parseHost(Request request) {
    request.host = MoreObjects.firstNonNull(request.host, request.getHeader(HttpHeaders.HOST_NAME));
    if (request.host.contains(Strings.COLON)) {
      String[] arr = request.host.split(Strings.COLON);
      request.domain = arr[0];
      request.port = Integer.parseInt(arr[1]);
    } else {
      request.domain = request.host;
      request.port = 80;
    }
  }

  private void parseContentType(Request request) {
    String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
    if (contentType != null) {
      String[] arr = contentType.split(Strings.SEMICOLON);
      request.contentType = arr[0];
      for (int i = 1; i < arr.length; i++) {
        String[] args = arr[i].split(Strings.EQUAL);
        if (args.length != 2) {
          continue;
        }
        String key = args[0].trim();
        String value = args[1].trim();
        if (key.equalsIgnoreCase(HttpHeaders.CHARSET)) {
          request.encoding = value;
        } else if (key.equalsIgnoreCase(HttpHeaders.BOUNDARY) && HttpMethod.POST == request
            .getMethod() && contentType.toLowerCase(Locale.ENGLISH)
            .startsWith(HttpHeaders.MULTIPART)) {
          request.multipart = new Multipart(value, request.getEncoding());
        }
      }
    }
  }

  private void parseCookie(Request request) {
    String cookieValue = request.getHeader(HttpHeaders.COOKIE);
    if (cookieValue == null || cookieValue.trim().length() == 0) {
      return;
    }
    Cookie cookie = null;
    for (String line : cookieValue.split(Strings.SEMICOLON)) {
      line = line.trim();
      int index = line.indexOf(Strings.EQUAL);
      if (index == -1 || line.length() <= 1) {
        continue;
      }
      String name = line.substring(0, index);
      String value = cookieValue(line.substring(index + 1));
      if (name.startsWith(Strings.DOLLAR)) {
        if (cookie != null && "$Path".equals(name)) {
          cookie.setPath(value);
        } else if (cookie != null && "$Domain".equals(name)) {
          cookie.setDomain(value);
        }
      } else {
        cookie = new Cookie();
        cookie.setName(name);
        cookie.setValue(value);
        request.getCookies().put(name, cookie);
      }
    }
  }

  private String cookieValue(String value) {
    if (value.startsWith(Strings.DOUBLE_QUOTATION_MARK) && value
        .endsWith(Strings.DOUBLE_QUOTATION_MARK) && value.length() > 1) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }
}
