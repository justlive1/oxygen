/*
 * Copyright (C) 2018 justlive1
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
package vip.justlive.oxygen.core.io;

import java.util.Properties;
import vip.justlive.oxygen.core.util.PlaceHolderHelper;

/**
 * 配置文件源
 *
 * @author wubo
 */
@FunctionalInterface
public interface PropertySource {

  /**
   * 获取属性集合
   *
   * @return 属性集合
   */
  Properties props();

  /**
   * 获取属性
   *
   * @param key 属性键值
   * @return 属性值
   */
  default String getProperty(String key) {
    String value = props().getProperty(key);
    if (value == null) {
      return null;
    }
    return PlaceHolderHelper.DEFAULT_HELPER.replacePlaceholders(value, props());
  }

  /**
   * 获取属性，可设置默认值
   *
   * @param key 属性键值
   * @param defaultValue 默认值
   * @return 属性值
   */
  default String getProperty(String key, String defaultValue) {
    String value = props().getProperty(key, defaultValue);
    if (value == null) {
      return null;
    }
    return PlaceHolderHelper.DEFAULT_HELPER.replacePlaceholders(value, props());
  }
}
