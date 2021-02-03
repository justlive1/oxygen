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

import java.util.Properties;
import vip.justlive.oxygen.core.Order;
import vip.justlive.oxygen.core.util.base.Naming;
import vip.justlive.oxygen.core.util.base.PlaceHolderHelper;

/**
 * 配置文件源
 *
 * @author wubo
 */
@FunctionalInterface
public interface PropertySource extends Order, Naming {

  /**
   * 获取属性集合
   *
   * @return 属性集合
   */
  Properties props();

  /**
   * 获取属性，如果遇到占位符会自动获取
   *
   * @param key 属性键值
   * @return 属性值
   */
  default String getProperty(String key) {
    String value = getRawProperty(key);
    if (value == null) {
      return null;
    }
    return getPlaceholderProperty(value);
  }

  /**
   * 获取属性值
   *
   * @param key 键
   * @return 属性值
   */
  default String getRawProperty(String key) {
    return props().getProperty(key);
  }

  /**
   * 获取属性，可设置默认值
   *
   * @param key 属性键值
   * @param defaultValue 默认值
   * @return 属性值
   */
  default String getProperty(String key, String defaultValue) {
    String value = getProperty(key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /**
   * 获取占位符属性
   *
   * @param placeholder 占位 eg. ${a}
   * @return 属性值
   */
  default String getPlaceholderProperty(String placeholder) {
    return PlaceHolderHelper.DEFAULT_HELPER.replacePlaceholders(placeholder, props());
  }

  /**
   * 是否包含该前缀属性
   *
   * @param prefix 前缀
   * @return true包含
   */
  default boolean containPrefix(String prefix) {
    Properties properties = props();
    for (Object key : properties.keySet()) {
      if (key.toString().startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  @Override
  default String name() {
    return getClass().getSimpleName();
  }
}
