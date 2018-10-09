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
 * text请求解析
 * <br>
 * 解析如下请求类型 application/xml,multipart/form-data,text/plain,text/xml,text/html
 *
 * @author wubo
 */
public class TextRequestParse extends AbstractRequestParse {

  @Override
  public boolean supported(HttpServletRequest req) {
    String contentType = Request.current().getContentType();

    return Constants.APPLICATION_XML.equalsIgnoreCase(contentType) || Constants.TEXT_HTML
        .equalsIgnoreCase(contentType) || Constants.TEXT_PLAIN.equalsIgnoreCase(contentType)
        || Constants.TEXT_XML.equalsIgnoreCase(contentType);
  }

  @Override
  public void handle(HttpServletRequest req) {
    margeParam(Request.current().getParams(), Constants.BODY_STORE_KEY, readBody(req));
  }
}
