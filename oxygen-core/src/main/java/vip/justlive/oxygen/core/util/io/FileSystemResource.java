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
package vip.justlive.oxygen.core.util.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.Urls;

/**
 * 文件系统资源，包括File，Path
 *
 * @author wubo
 */
public class FileSystemResource implements SourceResource {

  private final File file;
  private String filePath;
  private Path path;

  /**
   * 通过文件路径创建一个 {@code FileSystemResource}
   *
   * @param filePath 文件路径
   */
  public FileSystemResource(String filePath) {
    this.file = new File(MoreObjects.notNull(filePath));
    this.filePath = file.toPath().normalize().toString();
  }

  /**
   * 通过{@link File}创建一个 {@code FileSystemResource}
   *
   * @param file 文件
   */
  public FileSystemResource(File file) {
    this.file = MoreObjects.notNull(file);
  }

  /**
   * 通过{@link Path}创建一个 {@code FileSystemResource}
   *
   * @param path 路径
   */
  public FileSystemResource(Path path) {
    this.path = MoreObjects.notNull(path);
    this.file = path.toFile();
  }

  @Override
  public String path() {
    if (filePath == null) {
      filePath = file.toPath().normalize().toString();
    }
    return filePath;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (path == null) {
      path = file.toPath();
    }
    return Files.newInputStream(path);
  }

  @Override
  public boolean isFile() {
    return file.isFile();
  }

  @Override
  public File getFile() {
    return file;
  }

  @Override
  public URL getUrl() throws IOException {
    return file.toURI().toURL();
  }

  /**
   * 获取文件Path
   *
   * @return Path
   */
  public Path getPath() {
    if (path == null) {
      path = file.toPath();
    }
    return path;
  }

  @Override
  public SourceResource createRelative(String path) {
    return new FileSystemResource(Urls.relativePath(this.path(), path));
  }

}
