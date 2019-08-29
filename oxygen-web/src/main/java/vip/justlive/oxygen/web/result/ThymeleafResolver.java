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

import java.util.Locale;
import java.util.Map;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.template.Templates;
import vip.justlive.oxygen.web.WebConf;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;

/**
 * thymeleaf处理器
 *
 * @author wubo
 */
public class ThymeleafResolver {

  private final TemplateEngine templateEngine;

  ThymeleafResolver() {
    WebConf webConf = ConfigFactory.load(WebConf.class);
    this.templateEngine = new TemplateEngine();
    ResourceTemplateResolver resolver = new ResourceTemplateResolver(
        webConf.getThymeleafViewPrefix());
    resolver.setCacheable(webConf.isViewCacheEnabled());
    this.templateEngine.setTemplateResolver(resolver);
  }

  public String handler(Request request, Response response, String path, Map<String, Object> data) {
    return templateEngine.process(path,
        new WebContext(request.getOriginalRequest(), response.getOriginalResponse(),
            request.getOriginalRequest().getServletContext(), Locale.getDefault(), data));
  }

  /**
   * 资源模板解析
   */
  static class ResourceTemplateResolver extends StringTemplateResolver {

    private final String prefix;

    ResourceTemplateResolver(String prefix) {
      super();
      this.prefix = prefix;
      setName("Oxygen-Thymeleaf3");
    }

    @Override
    protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration,
        String ownerTemplate, String template, Map<String, Object> templateResolutionAttributes) {
      if (isCacheable()) {
        return new StringTemplateResource(Templates.cachedTemplate(prefix + template));
      }
      return new StringTemplateResource(Templates.template(prefix + template));
    }
  }
}
