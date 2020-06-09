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
package vip.justlive.oxygen.web.exception;

import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.oxygen.core.exception.ErrorCode;
import vip.justlive.oxygen.core.exception.WrappedException;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.Resp;
import vip.justlive.oxygen.core.util.json.Json;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.result.Result;
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
    if (log.isDebugEnabled()) {
      Throwable re = e;
      if (e instanceof WrappedException) {
        re = ((WrappedException) e).getException();
      }
      log.debug("Route handle error", re);
    }
    Resp resp = resp(e);
    ctx.response().setResult(Result.json(resp));
  }

  @Override
  public void error(RoutingContext ctx, Throwable e) {
    log.error("Handler processing failed", e);
    Response response = ctx.response();
    response.setContentType(HttpHeaders.APPLICATION_JSON);
    response.setStatus(500);
    response.write(Json.toJson(resp(e)));
  }

  private Resp resp(Throwable e) {
    if (e instanceof WrappedException) {
      e = ((WrappedException) e).getException();
    }
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
    return resp;
  }
}
