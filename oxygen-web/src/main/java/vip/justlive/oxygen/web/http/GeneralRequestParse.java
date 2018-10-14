/*
 * Copyright (C) 2018 justlive1
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

import javax.servlet.http.HttpServletRequest;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * 请求基础解析
 *
 * @author wubo
 */
public class GeneralRequestParse extends AbstractRequestParse {

  @Override
  public boolean supported(HttpServletRequest req) {
    return true;
  }

  @Override
  public void handle(HttpServletRequest req) {
    Request request = Request.current();
    request.queryString = req.getQueryString();
    request.method = req.getMethod().intern();
    request.path = req.getServletPath();
    request.contentPath = req.getContextPath();
    request.url = req.getRequestURL().toString();
    if (request.getQueryString() != null) {
      request.url = request.getUrl() + Constants.QUESTION_MARK + request.getQueryString();
    }
    String host = req.getHeader(Constants.HOST_NAME);
    if (host.contains(Constants.COLON)) {
      String[] arr = host.split(Constants.COLON);
      request.host = arr[0];
      request.port = Integer.parseInt(arr[1]);
    } else {
      request.host = host;
      request.port = 90;
    }
    request.remoteAddress = getIpAddress(request, req.getRemoteAddr());
  }

  private String getIpAddress(final Request request, String remoteIp) {
    String ip = request.getHeader(Constants.X_FORWARDED_FOR);
    if (checkIp(ip)) {
      return ip;
    }
    ip = request.getHeader(Constants.PROXY_CLIENT_IP);
    if (checkIp(ip)) {
      return ip;
    }
    ip = request.getHeader(Constants.WL_PROXY_CLIENT_IP);
    if (checkIp(ip)) {
      return ip;
    }
    ip = request.getHeader(Constants.X_REAL_IP);
    if (checkIp(ip)) {
      return ip;
    }
    ip = remoteIp;
    if (checkIp(ip)) {
      return ip;
    }
    // 应对x-forwarded-for 中返回多个服务器ip的情况
    if (ip != null && ip.contains(Constants.COLON)) {
      for (String a : ip.split(Constants.COLON)) {
        if (checkIp(ip)) {
          return a.trim();
        }
      }
    }
    return ip;
  }

  private boolean checkIp(String ip) {
    return ip != null && ip.length() > 0 && !Constants.UNKNOWN.equalsIgnoreCase(ip);
  }

}
