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

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 请求体包装，消息头指定消息长度和类型
 *
 * @author wubo
 */
@Data
@Accessors(chain = true)
public class LengthFrame {

  /**
   * 消息头基础长度（类型+数据大小）
   */
  public static final int BASE_LENGTH = 8;

  /**
   * 类型，-1固定为心跳类型
   */
  private int type;
  /**
   * 请求体
   */
  private byte[] body;
}
