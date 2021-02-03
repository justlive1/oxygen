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

import freemarker.cache.MruCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.ResourceBundle;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.web.WebConfigKeys;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * freemarker 视图处理
 *
 * @author wubo
 */
@Bean
public class FreemarkerResultHandler implements ResultHandler {

  private static final boolean SUPPORTED = ClassUtils.isPresent("freemarker.template.Template");
  private static final String SUFFIX = ".ftl";

  private FreemarkerResolver resolver;

  public FreemarkerResultHandler() {
    if (SUPPORTED) {
      resolver = new FreemarkerResolver();
    }
  }

  @Override
  public boolean support(Result result) {
    if (SUPPORTED && result != null && ViewResult.class.isAssignableFrom(result.getClass())) {
      ViewResult data = (ViewResult) result;
      return data.getPath() != null && data.getPath().endsWith(SUFFIX);
    }
    return false;
  }

  @Override
  public void apply(RoutingContext ctx, Result result) {
    ViewResult data = (ViewResult) result;
    Response response = ctx.response();
    response.setContentType(HttpHeaders.TEXT_HTML);
    resolver.resolve(response, data.getPath(), data.getData());
  }

  static class FreemarkerResolver {

    final Configuration cfg;

    FreemarkerResolver() {
      cfg = new Configuration(Configuration.getVersion());
      cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      cfg.setClassForTemplateLoading(Bootstrap.class,
          WebConfigKeys.VIEW_PREFIX_FREEMARKER.getValue());
      cfg.setNumberFormat(Strings.OCTOTHORP);
      if (WebConfigKeys.VIEW_CACHE.castValue(boolean.class)) {
        cfg.setCacheStorage(new MruCacheStorage(20, 250));
      } else {
        cfg.unsetCacheStorage();
      }
    }

    void resolve(Response response, String path, Map<String, Object> data) {
      Locale locale = ResourceBundle.currentThreadLocale();
      try {
        Template template = cfg.getTemplate(path, locale);
        template.process(data, new OutputStreamWriter(response.getOut(), StandardCharsets.UTF_8));
      } catch (Exception e) {
        throw Exceptions.wrap(e);
      }
    }
  }
}
