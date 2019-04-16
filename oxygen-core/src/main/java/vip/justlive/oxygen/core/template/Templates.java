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
package vip.justlive.oxygen.core.template;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.io.SimpleResourceLoader;
import vip.justlive.oxygen.core.io.SourceResource;
import vip.justlive.oxygen.core.util.SnowflakeIdWorker;

/**
 * 模板工具
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class Templates {

  private static final Map<String, Path> CACHE = new ConcurrentHashMap<>(4);

  private static final String TEMP_DIR;

  static {
    TEMP_DIR = ConfigFactory.load(CoreConf.class).getBaseTempDir() + "/templates";
    File dir = new File(TEMP_DIR);
    if (!dir.exists() && !dir.mkdirs()) {
      log.error("create temp templates dir [{}] error", dir.getAbsolutePath());
    } else {
      log.info("temp templates dir is [{}]", dir.getAbsolutePath());
    }
  }

  /**
   * 获取指定路径的模板
   *
   * @param path 路径
   * @return template
   */
  public static String template(String path) {
    try {
      SourceResource sourceResource = new SimpleResourceLoader(path);
      try (InputStream is = sourceResource.getInputStream()) {
        File savedFile = new File(TEMP_DIR, String.valueOf(SnowflakeIdWorker.defaultNextId()));
        Path templatePath = savedFile.toPath();
        Files.copy(is, templatePath);
        savedFile.deleteOnExit();
        CACHE.put(path, templatePath);
        return new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);
      }
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
    Path templatePath = CACHE.get(path);
    if (templatePath == null) {
      return template(path);
    }
    try {
      return new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

}
