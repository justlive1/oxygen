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
package vip.justlive.oxygen.web.result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.Urls;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.oxygen.web.WebConfigKeys;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;
import vip.justlive.oxygen.web.servlet.DispatcherServlet;

/**
 * jsp 视图处理
 *
 * @author wubo
 */
@Bean
public class JspViewResultHandler implements ResultHandler {

  private static final String SUFFIX = ".jsp";
  private final String jspPrefix;

  public JspViewResultHandler() {
    jspPrefix = WebConfigKeys.VIEW_PREFIX_JSP.getValue();
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
    if (request.getAttribute(Request.ORIGINAL_REQUEST) instanceof ChannelContext) {
      throw Exceptions.fail("Not servlet container, jsp unsupported");
    }
    new JspRender().render(request, ctx.response(), data);
  }

  private class JspRender {

    void render(Request request, Response response, ViewResult data) {
      HttpServletRequest req = (HttpServletRequest) request.getAttribute(Request.ORIGINAL_REQUEST);
      HttpServletResponse resp = (HttpServletResponse) request
          .getAttribute(Response.ORIGINAL_RESPONSE);
      data.getData().forEach(req::setAttribute);
      String path = Urls.concat(jspPrefix, data.getPath());
      DispatcherServlet.copyResponse(response, resp);
      try {
        req.getRequestDispatcher(path).forward(req, resp);
      } catch (Exception e) {
        throw Exceptions.wrap(e);
      }
    }
  }
}
