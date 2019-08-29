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
package vip.justlive.oxygen.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * 简单资源加载器
 *
 * @author wubo
 */
public class SimpleResourceLoader extends AbstractResourceLoader implements SourceResource {

  private final String path;

  public SimpleResourceLoader(String path) {
    this.path = path;
    this.loader = ClassUtils.getDefaultClassLoader();
    init();
  }

  @Override
  public void init() {
    this.resources.addAll(this.parse(this.path));
  }

  @Override
  public String path() {
    return path;
  }

  @Override
  public boolean isFile() {
    return this.resources.get(0).isFile();
  }

  @Override
  public File getFile() throws IOException {
    return this.resources.get(0).getFile();
  }

  @Override
  public URL getURL() throws IOException {
    return this.resources.get(0).getURL();
  }

  @Override
  public SourceResource createRelative(String path) throws IOException {
    return this.resources.get(0).createRelative(path);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return this.resources.get(0).getInputStream();
  }
}
