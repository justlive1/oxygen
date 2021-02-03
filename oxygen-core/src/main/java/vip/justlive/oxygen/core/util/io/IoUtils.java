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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.util.base.Bytes;
import vip.justlive.oxygen.core.util.base.SystemUtils;

/**
 * io utils
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class IoUtils {

  private final int BUFFER_SIZE = 4096;

  /**
   * 读完流且不处理
   *
   * @param input 输入流
   * @return 总字节数
   * @throws IOException io异常
   */
  public long drain(InputStream input) throws IOException {
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
  public long copy(InputStream input, OutputStream output) throws IOException {
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
  public long copy(InputStream input, Bytes bytes) throws IOException {
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
  public long copy(InputStream input, CopyOperate operate) throws IOException {
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
  public byte[] toBytes(InputStream input) throws IOException {
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
  public String toString(InputStream input) throws IOException {
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
  public String toString(InputStream input, Charset charset) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    copy(input, output);
    return output.toString(charset.name());
  }

  /**
   * 关闭失败不会抛出异常
   *
   * @param closeable 被关闭的对象
   */
  public void close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  /**
   * 关闭channel
   *
   * @param channel asynchronous channel
   */
  public void close(AsynchronousSocketChannel channel) {
    if (channel == null) {
      return;
    }
    try {
      channel.shutdownInput();
    } catch (IOException e) {
      log.error("close channel.input error", e);
    }
    try {
      channel.shutdownOutput();
    } catch (IOException e) {
      log.error("close channel.output error", e);
    }
    try {
      channel.close();
    } catch (IOException e) {
      log.error("close channel error", e);
    }
  }

  /**
   * 合并buffer
   *
   * @param buffers buffer集合
   * @return 合并后的buffer
   */
  public ByteBuffer composite(List<ByteBuffer> buffers) {
    int capacity = 0;
    for (ByteBuffer buffer : buffers) {
      capacity += buffer.remaining();
    }
    ByteBuffer ret = ByteBuffer.allocate(capacity);
    for (ByteBuffer buffer : buffers) {
      ret.put(buffer);
    }
    ret.position(0);
    ret.limit(ret.capacity());
    return ret;
  }

  /**
   * 创建一个channel
   *
   * @param channelGroup 群组
   * @return channel
   * @throws IOException io异常时抛出
   */
  public AsynchronousSocketChannel create(AsynchronousChannelGroup channelGroup)
      throws IOException {
    return create(channelGroup, new InetSocketAddress(SystemUtils.findAvailablePort()));
  }

  /**
   * 创建一个channel，指定绑定地址
   *
   * @param channelGroup 群组
   * @param address 绑定地址
   * @return channel
   * @throws IOException io异常时抛出
   */
  @SuppressWarnings("squid:S2095")
  public AsynchronousSocketChannel create(AsynchronousChannelGroup channelGroup,
      InetSocketAddress address) throws IOException {
    return AsynchronousSocketChannel.open(channelGroup)
        .setOption(StandardSocketOptions.TCP_NODELAY, true)
        .setOption(StandardSocketOptions.SO_REUSEADDR, true)
        .setOption(StandardSocketOptions.SO_KEEPALIVE, true).bind(address);
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
