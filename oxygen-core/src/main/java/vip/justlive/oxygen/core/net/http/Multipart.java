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

package vip.justlive.oxygen.core.net.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.Bytes;
import vip.justlive.oxygen.core.util.FileUtils;
import vip.justlive.oxygen.core.util.HttpHeaders;
import vip.justlive.oxygen.core.util.IoUtils;
import vip.justlive.oxygen.core.util.MoreObjects;
import vip.justlive.oxygen.core.util.SnowflakeIdWorker;
import vip.justlive.oxygen.core.util.Strings;

/**
 * multipart
 *
 * @author wubo
 */
@Getter
public class Multipart {

  private static final String DEFAULT_BOUNDARY = "----oxygen_" + SnowflakeIdWorker.defaultNextId();
  private static final String FORM_DATA = "Content-Disposition: form-data; name=\"%s\"";
  private static final String FORM_FILE_DATA = "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"";

  private final String boundary;
  private List<Part> parts;

  public Multipart() {
    this.boundary = DEFAULT_BOUNDARY;
  }

  public Multipart add(String name, String value) {
    if (parts == null) {
      parts = new ArrayList<>();
    }
    parts.add(new Part(name, value));
    return this;
  }

  public Multipart add(String name, File file) {
    return add(name, file, null);
  }

  public Multipart add(String name, File file, String filename) {
    if (parts == null) {
      parts = new ArrayList<>();
    }
    parts.add(new Part(name, file, filename));
    return this;
  }

  public byte[] toBytes(Charset charset) {
    if (parts == null || parts.isEmpty()) {
      return new byte[0];
    }

    Bytes bytes = new Bytes();
    for (Part part : parts) {
      if (part.isFile()) {
        appendFile(part, bytes, charset);
      } else {
        appendValue(part, bytes, charset);
      }
    }
    appendEnd(bytes, charset);
    return bytes.toArray();
  }

  private void appendEnd(Bytes bytes, Charset charset) {
    bytes.write(Strings.DASH).write(Strings.DASH).write(boundary, charset).write(Strings.DASH)
        .write(Strings.DASH).write(Bytes.CR).write(Bytes.LF);
  }

  private void appendValue(Part part, Bytes bytes, Charset charset) {
    bytes.write(Strings.DASH).write(Strings.DASH).write(boundary, charset).write(Bytes.CR)
        .write(Bytes.LF);
    bytes.write(String.format(FORM_DATA, part.getName()), charset).write(Bytes.CR).write(Bytes.LF);
    bytes.write(Bytes.CR).write(Bytes.LF);
    bytes.write(part.getValue(), charset).write(Bytes.CR).write(Bytes.LF);
  }

  private void appendFile(Part part, Bytes bytes, Charset charset) {
    bytes.write(Strings.DASH).write(Strings.DASH).write(boundary, charset).write(Bytes.CR)
        .write(Bytes.LF);
    bytes.write(String.format(FORM_FILE_DATA, part.getName(), part.getFilename()), charset)
        .write(Bytes.CR).write(Bytes.LF);
    bytes.write(HttpHeaders.CONTENT_TYPE).write(Bytes.COLON).write(Bytes.SPACE).write(MoreObjects
        .firstNonNull(FileUtils.parseMimeType(part.getFilename()),
            HttpHeaders.APPLICATION_OCTET_STREAM));
    bytes.write(Bytes.CR).write(Bytes.LF).write(Bytes.CR).write(Bytes.LF);
    try (InputStream input = Files.newInputStream(part.getFile().toPath())) {
      IoUtils.copy(input, bytes);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
    bytes.write(Bytes.CR).write(Bytes.LF);
  }

}
