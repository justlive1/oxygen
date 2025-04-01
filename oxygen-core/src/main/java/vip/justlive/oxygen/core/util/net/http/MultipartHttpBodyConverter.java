/*
 * Copyright (C) 2024 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.Bytes;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.io.FileUtils;
import vip.justlive.oxygen.core.util.io.IoUtils;

/**
 * multipart/form-data http body 转换期
 *
 * @author wubo
 * @since 3.0.10
 */
public class MultipartHttpBodyConverter implements HttpBodyConverter {

  private static final String FORM_DATA = "Content-Disposition: form-data; name=\"%s\"";
  private static final String FORM_FILE_DATA = "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"";

  @Override
  public boolean canWrite(HttpRequest request) {
    if (request.getHttpBody() == HttpBody.MULTIPART) {
      return true;
    }
    String contentType = request.getHeaders().get(HttpHeaders.CONTENT_TYPE);
    return contentType != null && contentType.startsWith(HttpHeaders.MULTIPART_FORM_DATA);
  }

  @Override
  public void write(HttpRequest request, OutputStream out) throws IOException {
    List<Part> multipart = request.getParts();
    if (request.getBody() != null) {
      List<Part> mps = new ArrayList<>();
      MoreObjects.beanToMap(request.getBody())
          .forEach((k, v) -> mps.add(new Part(k, v.toString())));
      if (multipart == null) {
        multipart = mps;
      } else {
        multipart.addAll(mps);
      }
    }

    if (multipart == null || multipart.isEmpty()) {
      return;
    }
    Charset charset = request.getCharset();
    Bytes bytes = new Bytes();
    for (Part part : multipart) {
      if (part.isFile()) {
        appendFile(part, bytes, charset);
      } else {
        appendValue(part, bytes, charset);
      }
    }
    appendEnd(bytes, charset);
    out.write(bytes.toArray());
  }

  private void appendEnd(Bytes bytes, Charset charset) {
    bytes.write(Strings.DASH).write(Strings.DASH).write(Strings.DEFAULT_BOUNDARY, charset)
        .write(Strings.DASH).write(Strings.DASH).write(Bytes.CR).write(Bytes.LF);
  }

  private void appendValue(Part part, Bytes bytes, Charset charset) {
    bytes.write(Strings.DASH).write(Strings.DASH).write(Strings.DEFAULT_BOUNDARY, charset)
        .write(Bytes.CR).write(Bytes.LF);
    bytes.write(String.format(FORM_DATA, part.getName()), charset).write(Bytes.CR).write(Bytes.LF);
    bytes.write(Bytes.CR).write(Bytes.LF);
    bytes.write(part.getValue(), charset).write(Bytes.CR).write(Bytes.LF);
  }

  private void appendFile(Part part, Bytes bytes, Charset charset) {
    bytes.write(Strings.DASH).write(Strings.DASH).write(Strings.DEFAULT_BOUNDARY, charset)
        .write(Bytes.CR).write(Bytes.LF);
    bytes.write(String.format(FORM_FILE_DATA, part.getName(), part.getFilename()), charset)
        .write(Bytes.CR).write(Bytes.LF);
    bytes.write(HttpHeaders.CONTENT_TYPE).write(Bytes.COLON).write(Bytes.SPACE).write(
        MoreObjects.firstNonNull(FileUtils.parseMimeType(part.getFilename()),
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
