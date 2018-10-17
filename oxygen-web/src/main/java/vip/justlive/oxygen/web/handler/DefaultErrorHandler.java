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
package vip.justlive.oxygen.web.handler;

import com.alibaba.fastjson.JSON;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.domain.Resp;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.oxygen.core.exception.ErrorCode;
import vip.justlive.oxygen.web.http.Request;

/**
 * 默认错误处理
 *
 * @author wubo
 */
@Slf4j
public class DefaultErrorHandler implements ErrorHandler {

  private final String errorPage;
  private final int errorCode;

  public DefaultErrorHandler(String errorPage, int errorCode) {
    this.errorPage = errorPage;
    this.errorCode = errorCode;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response) {
    try {
      if (Constants.XML_HTTP_REQUEST.equals(request.getHeader(Constants.X_REQUESTED_WITH))) {
        response.setContentType(Constants.APPLICATION_JSON);
        Resp resp;
        Request req = Request.current();
        Exception exception = null;
        if (req != null) {
          exception = req.getException();
        }
        if (exception instanceof CodedException) {
          ErrorCode error = ((CodedException) exception).getErrorCode();
          resp = Resp.error(error.getCode(), error.getMessage());
        } else {
          resp = Resp.error("error");
        }
        response.getWriter().print(JSON.toJSONString(resp));
        return;
      }
      if (errorPage == null || errorPage.length() == 0) {
        response.sendError(errorCode);
      } else {
        String redirectUrl = errorPage;
        if (!redirectUrl.startsWith(Constants.HTTP_PREFIX) && !redirectUrl
            .startsWith(Constants.HTTPS_PREFIX)) {
          redirectUrl = request.getContextPath() + errorPage;
        }
        response.sendRedirect(redirectUrl);
      }
    } catch (Exception e) {
      // nothing to do
      log.error("error handle failed ", e);
    }
  }

}
