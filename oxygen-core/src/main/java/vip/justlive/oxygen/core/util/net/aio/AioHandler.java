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

package vip.justlive.oxygen.core.util.net.aio;

import java.nio.ByteBuffer;

/**
 * aio处理逻辑
 *
 * @author wubo
 */
public interface AioHandler {

  /**
   * 将数据译码成buffer
   *
   * @param data 数据
   * @param channelContext channel上下文
   * @return buffer
   */
  ByteBuffer encode(Object data, ChannelContext channelContext);

  /**
   * 将buffer解码成原始数据
   *
   * @param buffer 译码数据
   * @param readableSize 可读数据大小
   * @param channelContext channel上下文
   * @return 解码数据
   */
  Object decode(ByteBuffer buffer, int readableSize, ChannelContext channelContext);

  /**
   * 处理解码后的数据
   *
   * @param data 数据
   * @param channelContext channel上下文
   */
  void handle(Object data, ChannelContext channelContext);

  /**
   * 创建一个心跳数据
   * <p>
   * 当返回null时表示框架层面不发送心跳
   * </p>
   *
   * @param channelContext channel上下文
   * @return 心跳数据
   */
  default Object beat(ChannelContext channelContext) {
    return null;
  }
}
