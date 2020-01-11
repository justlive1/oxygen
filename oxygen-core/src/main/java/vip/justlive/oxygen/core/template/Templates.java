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
package vip.justlive.oxygen.core.template;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.io.FirstResourceLoader;
import vip.justlive.oxygen.core.io.SourceResource;
import vip.justlive.oxygen.core.util.FileUtils;
import vip.justlive.oxygen.core.util.IoUtils;
import vip.justlive.oxygen.core.util.SnowflakeIdWorker;

/**
 * 模板工具
 *
 * @author wubo
 */
@UtilityClass
public class Templates {

  private static final Map<String, Path> CACHE = new ConcurrentHashMap<>(4);

  private static final File BASE_DIR;

  static {
    BASE_DIR = FileUtils.createTempDir("templates");
  }

  /**
   * 获取指定路径的模板
   *
   * @param path 路径
   * @return template
   */
  public static String template(String path) {
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
  public static String cachedTemplate(String path) {
    try {
      Path templatePath = CACHE.get(path);
      if (templatePath != null) {
        return new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);
      }

      String template = template(path);
      File savedFile = new File(BASE_DIR, String.valueOf(SnowflakeIdWorker.defaultNextId()));
      templatePath = savedFile.toPath();
      Files.copy(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8)), templatePath);
      CACHE.put(path, templatePath);
      return template;
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

}
