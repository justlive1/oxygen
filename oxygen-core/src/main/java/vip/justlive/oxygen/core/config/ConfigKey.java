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
package vip.justlive.oxygen.core.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.convert.DefaultConverterService;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * 属性
 *
 * @author wubo
 */
@Getter
@RequiredArgsConstructor
public class ConfigKey {

  private final String name;
  private final String defaultValue;

  public ConfigKey(String name) {
    this(name, null);
  }

  public String getValue() {
    return ConfigFactory.getProperty(name, defaultValue);
  }

  public String getValue(String defaultValue) {
    return Strings.firstOrNull(getValue(), defaultValue);
  }

  public <T> T castValue(Class<T> type) {
    String value = getValue();
    if (value == null) {
      return null;
    }
    if (type == String.class) {
      return type.cast(value);
    }
    return DefaultConverterService.sharedConverterService().convert(value, type);
  }

  public <T> T castValue(Class<T> type, T defaultValue) {
    return MoreObjects.firstOrNull(castValue(type), defaultValue);
  }
}
