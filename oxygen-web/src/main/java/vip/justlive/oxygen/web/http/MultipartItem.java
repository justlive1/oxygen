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
package vip.justlive.oxygen.web.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Data;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.io.SourceStream;
import vip.justlive.oxygen.core.util.Checks;

/**
 * multipart item
 *
 * @author wubo
 */
@Data
public class MultipartItem implements SourceStream {

  private String disposition;
  private Charset charset;
  private String name;
  private String filename;
  private String extension = Constants.EMPTY;
  private String contentType;
  private Path path;

  public void setFilename(String filename) {
    this.filename = filename;
    if (filename != null) {
      int index = filename.lastIndexOf(Constants.DOT);
      if (index > -1) {
        this.extension = filename.substring(index + 1);
      }
    }
  }

  /**
   * 转换成文件
   *
   * @param file file
   * @throws IOException 抛出异常
   */
  public void transferTo(File file) throws IOException {
    Checks.notNull(file);
    Files.copy(path, file.toPath());
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return Files.newInputStream(path);
  }
}
