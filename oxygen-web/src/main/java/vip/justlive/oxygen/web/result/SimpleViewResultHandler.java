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
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.template.SimpleTemplateEngine;
import vip.justlive.oxygen.core.template.TemplateEngine;
import vip.justlive.oxygen.core.template.Templates;
import vip.justlive.oxygen.web.WebConf;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * 使用内置简单模板引擎处理
 *
 * @author wubo
 * @see vip.justlive.oxygen.core.template.SimpleTemplateEngine
 */
public class SimpleViewResultHandler implements ResultHandler {

  private final String suffix;
  private final String prefix;
  private final TemplateEngine templateEngine;
  private final boolean viewCacheEnabled;

  public SimpleViewResultHandler() {
    WebConf webConf = ConfigFactory.load(WebConf.class);
    this.suffix = webConf.getSimpleViewSuffix();
    this.prefix = webConf.getSimpleViewPrefix();
    this.templateEngine = new SimpleTemplateEngine();
    this.viewCacheEnabled = webConf.isViewCacheEnabled();
  }

  @Override
  public boolean support(Result result) {
    if (result != null && ViewResult.class.isAssignableFrom(result.getClass())) {
      ViewResult data = (ViewResult) result;
      return data.getPath() != null && data.getPath().endsWith(suffix);
    }
    return false;
  }

  @Override
  public void apply(RoutingContext ctx, Result result) {
    ViewResult data = (ViewResult) result;
    Response response = ctx.response();
    response.setContentType(Constants.TEXT_HTML);
    String template;
    if (viewCacheEnabled) {
      template = Templates.cachedTemplate(prefix + data.getPath());
    } else {
      template = Templates.template(prefix + data.getPath());
    }
    response.write(templateEngine.render(template, data.getData()));
  }
}
