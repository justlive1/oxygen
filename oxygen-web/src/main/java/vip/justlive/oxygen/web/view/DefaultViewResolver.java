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

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 默认视图解析
 *
 * @author wubo
 */
public class DefaultViewResolver implements ViewResolver {

  @Override
  public boolean supported(Object data) {
    // 重定向
    if (data != null && data.getClass() == View.class && ((View) data).isRedirect()) {
      return true;
    }
    // null 或者 非view 返回json
    return data == null || data.getClass() != View.class;
  }

  @Override
  public void resolveView(HttpServletRequest request, HttpServletResponse response, Object data) {
    try {
      if (data != null && data.getClass() == View.class) {
        View view = (View) data;
        String redirectUrl = view.getPath();
        if (!redirectUrl.startsWith(Constants.HTTP_PREFIX) && !redirectUrl
            .startsWith(Constants.HTTPS_PREFIX)) {
          redirectUrl = request.getContextPath() + view.getPath();
        }
        response.sendRedirect(redirectUrl);
      } else {
        response.getWriter().print(JSON.toJSONString(data));
      }
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
