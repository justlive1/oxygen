/*
 * Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */

package vip.justlive.oxygen.core.net.aio.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.util.SystemUtils;

/**
 * 工具类
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class Utils {

  /**
   * 关闭channel
   *
   * @param channel asynchronous channel
   */
  public static void close(AsynchronousSocketChannel channel) {
    if (channel == null) {
      return;
    }
    try {
      channel.shutdownInput();
    } catch (IOException e) {
      log.error("关闭channel.input失败", e);
    }
    try {
      channel.shutdownOutput();
    } catch (IOException e) {
      log.error("关闭channel.output失败", e);
    }
    try {
      channel.close();
    } catch (IOException e) {
      log.error("关闭channel失败", e);
    }
  }

  /**
   * 合并buffer
   *
   * @param buffers buffer集合
   * @return 合并后的buffer
   */
  public static ByteBuffer composite(List<ByteBuffer> buffers) {
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
   * @param groupContext 上下文
   * @return channel
   * @throws IOException io异常时抛出
   */
  public static AsynchronousSocketChannel create(GroupContext groupContext) throws IOException {
    return AsynchronousSocketChannel.open(groupContext.getChannelGroup())
        .setOption(StandardSocketOptions.TCP_NODELAY, true)
        .setOption(StandardSocketOptions.SO_REUSEADDR, true)
        .setOption(StandardSocketOptions.SO_KEEPALIVE, true)
        .bind(new InetSocketAddress(SystemUtils.findAvailablePort()));
  }
}
