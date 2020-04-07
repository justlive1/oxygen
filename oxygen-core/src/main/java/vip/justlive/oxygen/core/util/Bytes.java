/*
 * Copyright (C) 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain spec copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package vip.justlive.oxygen.core.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * byte 数组 自动增长
 *
 * @author wubo
 * @since 2.1.2
 */
public class Bytes {

  public static final byte AND = '&';
  public static final byte ANY = '*';
  public static final byte CLOSE_BRACE = '}';
  public static final byte CLOSE_BRACKET = ']';
  public static final byte CLOSE_PAREN = ')';
  public static final byte COLON = ':';
  public static final byte COMMA = ',';
  public static final byte CR = '\r';
  public static final byte DASH = '-';
  public static final byte DOLLAR = '$';
  public static final byte DOT = '.';
  public static final byte DOUBLE_QUOTATION_MARK = '\"';
  public static final byte EQUAL = '=';
  public static final byte LF = '\n';
  public static final byte OCTOTHORP = '#';
  public static final byte OPEN_BRACE = '{';
  public static final byte OPEN_BRACKET = '[';
  public static final byte OPEN_PAREN = '(';
  public static final byte QUESTION_MARK = '?';
  public static final byte SEMICOLON = ';';
  public static final byte SLASH = '/';
  public static final byte SPACE = ' ';
  public static final byte UNDERSCORE = '_';

  protected byte[] buffer;
  protected int count;
  private int padding;

  /**
   * 默认大小和增长值都为32
   */
  public Bytes() {
    this(32);
  }

  /**
   * 增长值默认32
   *
   * @param size 默认大小
   */
  public Bytes(int size) {
    this(size, 32);
  }

  /**
   * 按指定大小和增长值构造
   *
   * @param size 默认大小
   * @param padding 增长值
   */
  public Bytes(int size, int padding) {
    if (size < 0) {
      throw Exceptions.fail("[size] should be positive");
    }
    this.buffer = new byte[size];
    this.padding = padding;
  }

  /**
   * 写入字节
   *
   * @param b 字节
   * @return Bytes
   */
  public synchronized Bytes write(byte b) {
    ensureCapacity(count + 1);
    buffer[count] = b;
    count++;
    return this;
  }

  /**
   * 写入字符串 UTF-8编码
   *
   * @param str 字符串
   * @return Bytes
   */
  public Bytes write(String str) {
    return write(str, StandardCharsets.UTF_8);
  }

  /**
   * 写入字符串
   *
   * @param str 字符串
   * @param charset 编码
   * @return Bytes
   */
  public Bytes write(String str, Charset charset) {
    if (str == null) {
      return this;
    }
    return write(str.getBytes(charset));
  }

  /**
   * 写入字节数组
   *
   * @param b 字节数组
   * @return Bytes
   */
  public Bytes write(byte[] b) {
    return write(b, 0, b.length);
  }

  /**
   * 写入字节数组指定边界
   *
   * @param b 字节数组
   * @param off 起始
   * @param len 长度
   * @return Bytes
   */
  public synchronized Bytes write(byte[] b, int off, int len) {
    if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) - b.length > 0)) {
      throw new IndexOutOfBoundsException();
    }
    ensureCapacity(count + len);
    System.arraycopy(b, off, buffer, count, len);
    count += len;
    return this;
  }

  /**
   * 重置
   */
  public synchronized void reset() {
    count = 0;
  }

  /**
   * 字节大小
   *
   * @return size
   */
  public synchronized int size() {
    return count;
  }

  @Override
  public synchronized String toString() {
    return toString(StandardCharsets.UTF_8);
  }

  /**
   * 转string
   *
   * @param charset 编码
   * @return string
   */
  public synchronized String toString(Charset charset) {
    return new String(buffer, 0, count, charset);
  }

  /**
   * 转换成字符数组
   *
   * @return byte[]
   */
  public synchronized byte[] toArray() {
    return Arrays.copyOf(buffer, count);
  }

  private void ensureCapacity(int minLength) {
    if (minLength < 0) {
      throw new OutOfMemoryError();
    }
    if (buffer.length < minLength) {
      int newSize = minLength + padding;
      if (newSize < 0) {
        newSize = Integer.MAX_VALUE;
      }
      buffer = Arrays.copyOf(buffer, newSize);
    }
  }

}
