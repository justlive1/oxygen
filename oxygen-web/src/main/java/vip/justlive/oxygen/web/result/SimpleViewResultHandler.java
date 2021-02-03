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

import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.template.Templates;
import vip.justlive.oxygen.web.WebConfigKeys;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * 使用内置简单模板引擎处理
 *
 * @author wubo
 * @see Templates
 */
@Bean
public class SimpleViewResultHandler implements ResultHandler {

  private final String suffix;
  private final String prefix;
  private final boolean viewCacheEnabled;

  public SimpleViewResultHandler() {
    this.suffix = WebConfigKeys.VIEW_SUFFIX_SIMPLE.getValue();
    this.prefix = WebConfigKeys.VIEW_PREFIX_SIMPLE.getValue();
    this.viewCacheEnabled = WebConfigKeys.VIEW_CACHE.castValue(boolean.class);
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
    response.setContentType(HttpHeaders.TEXT_HTML);
    if (viewCacheEnabled) {
      response.write(Templates.loadAndRender(prefix + data.getPath(), data.getData()));
    } else {
      response.write(Templates.render(Templates.template(prefix + data.getPath()), data.getData()));
    }
  }
}
