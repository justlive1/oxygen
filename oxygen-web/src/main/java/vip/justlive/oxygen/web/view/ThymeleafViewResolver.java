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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * thymeleaf视图解析
 *
 * @author wubo
 */
@Slf4j
public class ThymeleafViewResolver implements ViewResolver {

  private static final boolean THYMELEAF_SUPPORTED = ClassUtils
      .isPresent("org.thymeleaf.Thymeleaf");
  private static final String SUFFIX = ".html";

  private ThymeleafHandler handler;

  private synchronized void check() {
    if (handler == null) {
      handler = new ThymeleafHandler();
    }
  }

  @Override
  public boolean supported(Object data) {
    if (!THYMELEAF_SUPPORTED) {
      return false;
    }
    if (data != null && data.getClass() == View.class) {
      View view = (View) data;
      return !view.isRedirect() && view.getPath().endsWith(SUFFIX);
    }
    return false;
  }

  @Override
  public void resolveView(HttpServletRequest request, HttpServletResponse response, Object data) {
    check();
    View view = (View) data;
    try {
      response.setContentType(Constants.TEXT_HTML);
      handler.handler(view.getPath(), view.getData(), response.getWriter());
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }


}
