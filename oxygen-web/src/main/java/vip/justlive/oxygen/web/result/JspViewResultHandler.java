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
package vip.justlive.oxygen.web.result;

import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ResourceUtils;
import vip.justlive.oxygen.web.WebConf;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * jsp 视图处理
 *
 * @author wubo
 */
public class JspViewResultHandler implements ResultHandler {

  private static final String SUFFIX = ".jsp";
  private final String jspPrefix;

  public JspViewResultHandler() {
    jspPrefix = ConfigFactory.load(WebConf.class).getJspViewPrefix();
  }

  @Override
  public boolean support(Result result) {
    if (result != null && ViewResult.class.isAssignableFrom(result.getClass())) {
      ViewResult data = (ViewResult) result;
      return data.getPath() != null && data.getPath().endsWith(SUFFIX);
    }
    return false;
  }

  @Override
  public void apply(RoutingContext ctx, Result result) {
    ViewResult data = (ViewResult) result;
    Request request = ctx.request();
    Response response = ctx.response();
    data.getData().forEach(request.getOriginalRequest()::setAttribute);
    String path = ResourceUtils.concat(jspPrefix, data.getPath());
    try {
      request.getOriginalRequest().getRequestDispatcher(path)
          .forward(request.getOriginalRequest(), response.getOriginalResponse());
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }
}
