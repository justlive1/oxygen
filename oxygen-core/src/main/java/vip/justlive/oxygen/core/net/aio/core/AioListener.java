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

package vip.justlive.oxygen.core.net.aio.core;

/**
 * aio监听器
 *
 * @author wubo
 */
public interface AioListener {

  /**
   * channel成功建立后触发
   *
   * @param channelContext channel上下文
   */
  default void onConnected(ChannelContext channelContext) {
  }

  /**
   * channel关闭时触发
   *
   * @param channelContext channel上下文
   */
  default void onClosed(ChannelContext channelContext) {

  }

  /**
   * channel解码数据处理后触发
   *
   * @param channelContext channel上下文
   * @param data 解码的数据
   */
  default void onHandled(ChannelContext channelContext, Object data) {
  }
}
