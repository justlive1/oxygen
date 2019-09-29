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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.net.aio.core.ChannelContext;
import vip.justlive.oxygen.core.template.Templates;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.HttpHeaders;
import vip.justlive.oxygen.core.util.ResourceBundle;
import vip.justlive.oxygen.ioc.annotation.Bean;
import vip.justlive.oxygen.web.WebConf;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * thymeleaf 视图处理
 *
 * @author wubo
 */
@Bean
public class ThymeleafViewResultHandler implements ResultHandler {

  private static final boolean SUPPORTED = ClassUtils.isPresent("org.thymeleaf.Thymeleaf");

  private final String suffix;
  private ThymeleafResolver resolver;

  public ThymeleafViewResultHandler() {
    this.suffix = ConfigFactory.load(WebConf.class).getThymeleafViewSuffix();
    if (SUPPORTED) {
      resolver = new ThymeleafResolver();
    }
  }

  @Override
  public boolean support(Result result) {
    if (SUPPORTED && result != null && ViewResult.class.isAssignableFrom(result.getClass())) {
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
    response.write(resolver.resolve(ctx.request(), data.getPath(), data.getData()));
  }

  class ThymeleafResolver {

    private final TemplateEngine templateEngine;

    ThymeleafResolver() {
      WebConf webConf = ConfigFactory.load(WebConf.class);
      this.templateEngine = new TemplateEngine();
      ResourceTemplateResolver templateResolver = new ResourceTemplateResolver(
          webConf.getThymeleafViewPrefix());
      templateResolver.setCacheable(webConf.isViewCacheEnabled());
      this.templateEngine.setTemplateResolver(templateResolver);
    }

    String resolve(Request request, String path, Map<String, Object> data) {
      IContext context;
      Locale locale = ResourceBundle.currentThreadLocale();
      if (request.getAttribute(Request.ORIGINAL_REQUEST) instanceof ChannelContext) {
        context = new Context(locale, data);
      } else {
        context = new WebContentBuilder().build(request, locale, data);
      }
      return templateEngine.process(path, context);
    }

    class WebContentBuilder {

      WebContext build(Request request, Locale locale, Map<String, Object> data) {
        HttpServletRequest req = (HttpServletRequest) request
            .getAttribute(Request.ORIGINAL_REQUEST);
        HttpServletResponse resp = (HttpServletResponse) request
            .getAttribute(Response.ORIGINAL_RESPONSE);
        return new WebContext(req, resp, req.getServletContext(), locale, data);
      }
    }

    /**
     * 资源模板解析
     */
    class ResourceTemplateResolver extends StringTemplateResolver {

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
}
