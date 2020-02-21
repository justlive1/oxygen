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

package vip.justlive.oxygen.core.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import lombok.experimental.UtilityClass;

/**
 * io utils
 *
 * @author wubo
 */
@UtilityClass
public class IoUtils {

  private static final int BUFFER_SIZE = 4096;

  /**
   * 读完流且不处理
   *
   * @param input 输入流
   * @return 总字节数
   * @throws IOException io异常
   */
  public static long drain(InputStream input) throws IOException {
    long byteCount = 0;
    if (input == null) {
      return byteCount;
    }
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;
    while ((bytesRead = input.read(buffer)) != -1) {
      byteCount += bytesRead;
    }
    return byteCount;
  }

  /**
   * copy
   *
   * @param input 输入流
   * @param output 输出流
   * @return 总字节数
   * @throws IOException io异常
   */
  public static long copy(InputStream input, OutputStream output) throws IOException {
    return copy(input, (buffer, bytesRead) -> output.write(buffer, 0, bytesRead));
  }

  /**
   * copy
   *
   * @param input 输入流
   * @param bytes 字节组
   * @return 总字节数
   * @throws IOException io异常
   */
  public static long copy(InputStream input, Bytes bytes) throws IOException {
    return copy(input, (buffer, bytesRead) -> bytes.write(buffer, 0, bytesRead));
  }

  /**
   * copy
   *
   * @param input 输入流
   * @param operate 操作单元
   * @return 总字节数
   * @throws IOException io异常
   */
  public static long copy(InputStream input, CopyOperate operate) throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];
    long count = 0;
    int bytesRead;
    while ((bytesRead = input.read(buffer)) != -1) {
      operate.copy(buffer, bytesRead);
      count += bytesRead;
    }
    return count;
  }

  /**
   * 输入流转byte[]
   *
   * @param input 输入流
   * @return byte[]
   * @throws IOException io异常
   */
  public static byte[] toBytes(InputStream input) throws IOException {
    Bytes bytes = new Bytes();
    copy(input, bytes);
    return bytes.toArray();
  }

  /**
   * 输入流转字符串
   *
   * @param input 输入流
   * @return 字符串
   * @throws IOException io异常
   */
  public static String toString(InputStream input) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    copy(input, output);
    return output.toString();
  }

  /**
   * 输入流转字符串
   *
   * @param input 输入流
   * @param charset 字符集
   * @return 字符串
   * @throws IOException io异常
   */
  public static String toString(InputStream input, Charset charset) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    copy(input, output);
    return output.toString(charset.name());
  }

  /**
   * 关闭失败不会抛出异常
   *
   * @param closeable 被关闭的对象
   */
  public static void close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  @FunctionalInterface
  interface CopyOperate {

    /**
     * copy
     *
     * @param buffer 数据
     * @param readBytes 数据字节数
     * @throws IOException io异常
     */
    void copy(byte[] buffer, int readBytes) throws IOException;
  }
}
