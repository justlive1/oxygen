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
package vip.justlive.oxygen.web.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import vip.justlive.oxygen.core.util.Bytes;
import vip.justlive.oxygen.core.util.FileUtils;
import vip.justlive.oxygen.core.util.HttpHeaders;
import vip.justlive.oxygen.core.util.SnowflakeIdWorker;
import vip.justlive.oxygen.core.util.Strings;

/**
 * multipart 流解析
 *
 * @author wubo
 */
class MultipartStream {

  private static final byte[] LINE_SEPARATOR = {Bytes.CR, Bytes.LF};
  private static final byte[] STREAM_TERMINATOR = {Bytes.DASH, Bytes.DASH};

  private final InputStream inputStream;
  private final byte[] boundary;
  private final byte[] endOfBoundary;
  private final Charset charset;
  Map<String, String> formData;
  List<MultipartItem> items = new LinkedList<>();
  private WrapByteArrayOutputStream current = new WrapByteArrayOutputStream();

  MultipartStream(InputStream inputStream, byte[] boundary, String encoding) throws IOException {
    this.inputStream = inputStream;
    this.charset = Charset.forName(encoding);
    current.reset();
    current.write(STREAM_TERMINATOR);
    current.write(boundary);
    this.boundary = current.toByteArray();
    current.write(STREAM_TERMINATOR);
    this.endOfBoundary = current.toByteArray();
    current.reset();
    formData = new HashMap<>(4);
  }

  void readMultipartItem() throws IOException {
    // boundary
    byte[] line = readLine();
    if (line.length == 0 || !Arrays.equals(line, boundary)) {
      return;
    }
    // disposition
    line = readLine();
    String[] disposition = parseDisposition(line);
    // contentType or crlf
    line = readLine();
    boolean isFile = line.length > 0;
    String contentType = null;
    if (isFile) {
      contentType = parseContentType(line);
      // crlf
      readLine();
    }
    // body
    line = readLine();
    if (!isFile) {
      formData.put(disposition[1], new String(line, charset));
    } else {
      MultipartItem item = new MultipartItem();
      item.setContentType(contentType);
      item.setDisposition(disposition[0]);
      item.setName(disposition[1]);
      item.setFilename(disposition[2]);
      item.setCharset(charset);
      readFile(line, item);
      items.add(item);
    }
    readMultipartItem();
  }

  private String[] parseDisposition(byte[] bytes) {
    String disposition = new String(bytes, charset);
    String name = null;
    String filename = null;
    for (String line : disposition.split(Strings.SEMICOLON)) {
      String[] props = line.split(Strings.EQUAL);
      if (props.length == 2) {
        String key = props[0].trim();
        String val = props[1].replace(Strings.DOUBLE_QUOTATION_MARK, Strings.EMPTY).trim();
        if (HttpHeaders.FORM_DATA_NAME.equals(key)) {
          name = val;
        } else if (HttpHeaders.FORM_DATA_FILENAME.equals(key)) {
          filename = val;
        }
      }
    }
    return new String[]{disposition, name, filename};
  }

  private String parseContentType(byte[] line) {
    String[] arr = new String(line, charset).split(Strings.COLON);
    if (arr.length > 1) {
      return arr[1];
    }
    return null;
  }


  private byte[] readLine() throws IOException {
    current.reset();
    while (true) {
      int read = inputStream.read();
      // 结束
      if (read == -1) {
        return current.toByteArray();
      }
      // 读取到行中断
      if (read == LINE_SEPARATOR[1] && current.size() > 0
          && current.byteOfIndex(current.size() - 1) == LINE_SEPARATOR[0]) {
        current.removeLastByte();
        return current.toByteArray();
      } else {
        current.write(read);
      }
    }
  }

  private void readFile(byte[] line, MultipartItem item) throws IOException {
    Path path = new File(FileUtils.createTempDir(MultipartItem.class.getSimpleName()),
        String.valueOf(SnowflakeIdWorker.defaultNextId())).toPath();
    item.setPath(path);
    while (true) {
      Files.write(path, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      line = readLine();
      if (Arrays.equals(line, boundary) || Arrays.equals(line, endOfBoundary)) {
        break;
      }
      Files.write(path, LINE_SEPARATOR, StandardOpenOption.APPEND);
    }
  }

  static class WrapByteArrayOutputStream extends ByteArrayOutputStream {

    /**
     * 当前下标的byte
     *
     * @param index 下标
     * @return byte
     */
    int byteOfIndex(int index) {
      if (index < 0 || index >= count) {
        return -1;
      }
      return buf[index];
    }

    /**
     * 删除最后一个byte
     */
    void removeLastByte() {
      count--;
    }
  }

}

