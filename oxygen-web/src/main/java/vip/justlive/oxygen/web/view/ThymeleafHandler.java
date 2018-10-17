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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.io.SimpleResourceLoader;
import vip.justlive.oxygen.core.io.SourceResource;
import vip.justlive.oxygen.core.util.SnowflakeIdWorker;
import vip.justlive.oxygen.web.WebConf;

/**
 * thymeleaf handler
 *
 * @author wubo
 */
@Slf4j
public class ThymeleafHandler {

  private static final Map<String, Path> CACHE = new ConcurrentHashMap<>(4);
  private final TemplateEngine templateEngine;

  ThymeleafHandler() {
    templateEngine = new TemplateEngine();
    String tempDir = ConfigFactory.load(CoreConf.class).getBaseTempDir() + "/thymeleaf";
    File dir = new File(tempDir);
    if (!dir.exists() && !dir.mkdirs()) {
      log.error("create temp thymeleaf dir [{}] error", dir.getAbsolutePath());
    } else {
      log.info("temp thymeleaf dir is [{}]", dir.getAbsolutePath());
    }
    WebConf webConf = ConfigFactory.load(WebConf.class);
    ResourceTemplateResolver resolver = new ResourceTemplateResolver(webConf.getThymeleafPrefix(),
        tempDir);
    resolver.setCacheable(webConf.isViewCacheEnabled());
    templateEngine.setTemplateResolver(resolver);
  }

  public void handler(String path, Map<String, Object> data, Writer writer) {
    templateEngine.process(path, new WebContext(data, Locale.getDefault()), writer);
  }

  /**
   * 资源模板解析
   */
  public static class ResourceTemplateResolver extends StringTemplateResolver {

    private final String prefix;
    private final String tempDir;

    ResourceTemplateResolver(String prefix, String tempDir) {
      super();
      setName("Oxygen-Thymeleaf3");
      this.prefix = prefix;
      this.tempDir = tempDir;
    }

    @Override
    protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration,
        String ownerTemplate, String template, Map<String, Object> templateResolutionAttributes) {
      Path path = CACHE.get(template);
      String source;
      InputStream is = null;
      try {
        if (path == null) {
          SourceResource sourceResource = new SimpleResourceLoader(prefix + template);
          is = sourceResource.getInputStream();
          File savedFile = new File(tempDir, String.valueOf(SnowflakeIdWorker.defaultNextId()));
          path = savedFile.toPath();
          Files.copy(is, path);
          savedFile.deleteOnExit();
        }
        source = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw Exceptions.wrap(e);
      } finally {
        try {
          if (isCacheable()) {
            CACHE.putIfAbsent(template, path);
          } else {
            Files.deleteIfExists(path);
          }
          if (is != null) {
            is.close();
          }
        } catch (IOException e) {
          // nothing
        }
      }
      return new StringTemplateResource(source);
    }
  }

  public static class WebContext implements IContext {

    private final Map<String, Object> data;
    private final Locale locale;

    WebContext(Map<String, Object> data, Locale locale) {
      this.data = data;
      if (locale == null) {
        this.locale = Locale.getDefault();
      } else {
        this.locale = locale;
      }
    }

    @Override
    public java.util.Locale getLocale() {
      return locale;
    }

    @Override
    public boolean containsVariable(String name) {
      return data.containsKey(name);
    }

    @Override
    public Set<String> getVariableNames() {
      return data.keySet();
    }

    @Override
    public Object getVariable(String name) {
      return data.get(name);
    }
  }
}
