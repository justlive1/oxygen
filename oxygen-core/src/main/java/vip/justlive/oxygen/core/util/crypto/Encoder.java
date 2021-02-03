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
package vip.justlive.oxygen.core.util.crypto;

/**
 * 加密接口
 *
 * @author wubo
 */
public interface Encoder {

  /**
   * 加密
   *
   * @param source 源
   * @return 加密字符串
   */
  String encode(String source);

  /**
   * 是否匹配
   *
   * @param source 源
   * @param raw 加密后数据
   * @return true表示匹配
   */
  boolean match(String source, String raw);
}
