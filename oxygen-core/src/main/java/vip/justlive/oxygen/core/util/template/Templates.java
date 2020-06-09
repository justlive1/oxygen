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
package vip.justlive.oxygen.core.util.template;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.SnowflakeId;
import vip.justlive.oxygen.core.util.io.FileUtils;
import vip.justlive.oxygen.core.util.io.FirstResourceLoader;
import vip.justlive.oxygen.core.util.io.IoUtils;
import vip.justlive.oxygen.core.util.io.SourceResource;

/**
 * 模板工具
 *
 * @author wubo
 */
@UtilityClass
public class Templates {

  private final Map<String, Path> CACHE = new ConcurrentHashMap<>(4);
  private final TemplateEngine ENGINE = new SimpleTemplateEngine();

  private final File BASE_DIR;

  static {
    BASE_DIR = FileUtils.createTempDir("templates");
  }

  /**
   * 获取指定路径的模板
   *
   * @param path 路径
   * @return template
   */
  public String template(String path) {
    SourceResource sourceResource = new FirstResourceLoader(path).getResource();
    if (sourceResource == null) {
      throw Exceptions.fail(String.format("template [%s] not found", path));
    }
    try (InputStream is = sourceResource.getInputStream()) {
      return IoUtils.toString(is, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  /**
   * 获取缓存的模板
   *
   * @param path 路径
   * @return template
   */
  public String cachedTemplate(String path) {
    try {
      Path templatePath = CACHE.get(path);
      if (templatePath != null) {
        return new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);
      }

      String template = template(path);
      File savedFile = new File(BASE_DIR, String.valueOf(SnowflakeId.defaultNextId()));
      templatePath = savedFile.toPath();
      Files.copy(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8)), templatePath);
      CACHE.put(path, templatePath);
      return template;
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public String loadAndRender(String path, Map<String, Object> attrs) {
    return render(cachedTemplate(path), attrs);
  }

  public void loadAndRender(String path, Map<String, Object> attrs, Writer writer) {
    render(cachedTemplate(path), attrs, writer);
  }

  public String render(String template, Map<String, Object> attrs) {
    return ENGINE.render(template, attrs);
  }

  public void render(String template, Map<String, Object> attrs, Writer writer) {
    ENGINE.render(template, attrs, writer);
  }
}
