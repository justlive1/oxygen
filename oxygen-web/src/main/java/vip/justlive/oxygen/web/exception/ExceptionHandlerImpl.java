/*
 *  Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */
package vip.justlive.oxygen.web.exception;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.domain.Resp;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.oxygen.core.exception.ErrorCode;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * 默认异常处理
 *
 * @author wubo
 */
@Slf4j
public class ExceptionHandlerImpl implements ExceptionHandler {

  @Override
  public void handle(RoutingContext ctx, Exception e, int status) {
    Response response = ctx.response();
    Resp resp = null;
    if (e instanceof CodedException) {
      ErrorCode errorCode = ((CodedException) e).getErrorCode();
      if (errorCode != null) {
        resp = Resp.error(errorCode.getCode(), errorCode.getMessage());
      }
    }
    if (resp == null) {
      resp = Resp.error(e.toString());
    }
    response.setContentType(Constants.APPLICATION_JSON);
    response.setStatus(status);
    try {
      response.getOut()
          .write(JSON.toJSONString(resp).getBytes(Charset.forName(response.getEncoding())));
    } catch (IOException e1) {
      log.error("error handler ", e1);
    }
  }
}
