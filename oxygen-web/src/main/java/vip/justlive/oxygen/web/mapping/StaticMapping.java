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
package vip.justlive.oxygen.web.mapping;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.io.SimpleResourceLoader;
import vip.justlive.oxygen.core.io.SourceResource;
import vip.justlive.oxygen.core.util.ExpiringMap;
import vip.justlive.oxygen.core.util.SnowflakeIdWorker;

/**
 * 静态资源映射
 *
 * @author wubo
 */
@Slf4j
public class StaticMapping {

  private static final String TEMP_DIR;
  private static final Properties MIME_TYPES = new Properties();

  static {
    try {
      MIME_TYPES.load(StaticMapping.class.getResourceAsStream("/mime-types.properties"));
    } catch (IOException e) {
      log.warn("mime types initial failed ", e);
    }
    TEMP_DIR = ConfigFactory.load(CoreConf.class).getBaseTempDir() + "/static";
    File dir = new File(TEMP_DIR);
    if (!dir.exists() && !dir.mkdirs()) {
      log.error("create temp static dir [{}] error", dir.getAbsolutePath());
    } else {
      log.info("temp static dir is [{}]", dir.getAbsolutePath());
    }
  }

  private final Map<String, Set<String>> resources;
  private final ExpiringMap<String, StaticSource> expiringMap;

  public StaticMapping() {
    resources = new ConcurrentHashMap<>(4, 1f);
    expiringMap = ExpiringMap.<String, StaticSource>builder().expiration(10, TimeUnit.MINUTES)
        .asyncExpiredListeners(this::cleanExpiredFile).build();
  }

  private void cleanExpiredFile(String key, StaticSource source) {
    log.info("static mapping cached source expired for [{}] [{}]", key, source);
    if (source != null && source.path != null) {
      try {
        Files.deleteIfExists(source.path);
      } catch (IOException e) {
        log.error("delete file error,", e);
      }
    }
  }

  /**
   * 增加静态资源
   *
   * @param prefix 请求前缀
   * @param basePath base路径
   */
  public void addStaticResource(String prefix, String basePath) {
    Set<String> paths = resources.get(prefix);
    if (paths == null) {
      resources.putIfAbsent(prefix, new HashSet<>(2));
    }
    paths = resources.get(prefix);
    paths.add(basePath);
  }

  /**
   * 根据路径查找静态资源
   *
   * @param path 请求路径
   * @return StaticSource
   */
  public StaticSource findStaticResource(String path) {
    StaticSource cachedSource = expiringMap.get(path);
    if (cachedSource != null) {
      return cachedSource;
    }

    for (Map.Entry<String, Set<String>> entry : resources.entrySet()) {
      if (path.startsWith(entry.getKey())) {
        for (String value : entry.getValue()) {
          StaticSource source = findMappedSource(path, entry.getKey(), value);
          if (source != null) {
            expiringMap.put(path, source);
            return source;
          }
        }
        break;
      }
    }
    return null;
  }

  private StaticSource findMappedSource(String path, String prefix, String basePath) {
    String filePath = basePath + path.substring(prefix.length());
    InputStream is = null;
    try {
      SourceResource sourceResource = new SimpleResourceLoader(filePath);
      File file = sourceResource.getFile();
      if (file != null && file.isDirectory()) {
        return null;
      }
      is = sourceResource.getInputStream();
      File savedFile = new File(TEMP_DIR, String.valueOf(SnowflakeIdWorker.defaultNextId()));
      Files.copy(is, savedFile.toPath());
      savedFile.deleteOnExit();
      return new StaticSource(savedFile, path);
    } catch (IOException e) {
      // not found or error happens ignore
      return null;
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // nothing
        }
      }
    }
  }

  /**
   * 静态资源异常 用于中断
   */
  @Getter
  public static class StaticException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final transient StaticSource source;

    public StaticException(StaticSource source) {
      super(null, null, false, false);
      this.source = source;
    }
  }

  /**
   * 静态资源
   */
  @Getter
  public class StaticSource {

    private final Path path;
    private final String contentType;
    private final String requestPath;

    StaticSource(File file, String requestPath) {
      this.path = file.toPath();
      this.requestPath = requestPath;
      String suffix = requestPath.substring(requestPath.lastIndexOf(Constants.DOT) + 1);
      contentType = MIME_TYPES.getProperty(suffix, Constants.APPLICATION_OCTET_STREAM);
    }

    /**
     * 修改时间
     *
     * @return lastModified
     */
    public long lastModified() {
      // 去除毫秒值
      return path.toFile().lastModified() / 1000 * 1000;
    }

    /**
     * etag
     *
     * @return etag
     */
    public String eTag() {
      return Constants.DOUBLE_QUOTATION_MARK + lastModified() + Constants.HYPHEN + path.hashCode()
          + Constants.DOUBLE_QUOTATION_MARK;
    }
  }

}
