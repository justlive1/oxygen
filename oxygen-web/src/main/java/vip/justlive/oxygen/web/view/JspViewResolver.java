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
package vip.justlive.oxygen.web.view;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.web.WebConf;

/**
 * jsp视图解析
 *
 * @author wubo
 */
public class JspViewResolver implements ViewResolver {

  private static final String SUFFIX = ".jsp";
  private final String jspPrefix;

  public JspViewResolver() {
    jspPrefix = ConfigFactory.load(WebConf.class).getJspPrefix();
  }

  @Override
  public boolean supported(Object data) {
    if (data != null && data.getClass() == View.class) {
      View view = (View) data;
      return !view.isRedirect() && view.getPath().endsWith(SUFFIX);
    }
    return false;
  }

  @Override
  public void resolveView(HttpServletRequest request, HttpServletResponse response, Object data) {
    try {
      View view = (View) data;
      if (view.getData() != null) {
        view.getData().forEach(request::setAttribute);
      }
      request.getRequestDispatcher(getPath(view.getPath())).forward(request, response);
    } catch (ServletException | IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private String getPath(String path) {
    StringBuilder sb = new StringBuilder(jspPrefix);
    if (!jspPrefix.endsWith(Constants.ROOT_PATH)) {
      sb.append(Constants.ROOT_PATH);
    }
    if (path.startsWith(Constants.ROOT_PATH)) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.append(path).toString();
  }
}
