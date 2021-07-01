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

package vip.justlive.oxygen.core.util.net.http;

import java.io.File;
import lombok.Getter;
import vip.justlive.oxygen.core.util.base.MoreObjects;

/**
 * part
 *
 * @author wubo
 */
@Getter
public class Part {

  private final String name;
  private final String value;
  private final File file;
  private String filename;

  public Part(String name, String value) {
    this.name = MoreObjects.notNull(name);
    this.value = MoreObjects.notNull(value);
    this.file = null;
  }

  public Part(String name, File file, String filename) {
    this.name = MoreObjects.notNull(name);
    this.file = MoreObjects.notNull(file);
    this.filename = MoreObjects.firstNonNull(filename, file.getName());
    this.value = null;
  }

  public boolean isFile() {
    return file != null;
  }

  @Override
  public String toString() {
    String res = "{name:" + name + ", value:" + value;
    if (isFile()) {
      res += ", file:" + file + ", filename:" + filename;
    }
    res += "}";
    return res;
  }
}
