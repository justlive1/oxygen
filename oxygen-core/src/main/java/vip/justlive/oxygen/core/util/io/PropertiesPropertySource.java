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
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Properties配置属性源
 *
 * @author wubo
 */
@Accessors(chain = true)
public class PropertiesPropertySource implements PropertySource {

  private final Properties properties = new Properties();
  @Setter
  private int order;

  public void setProperty(String key, String value) {
    properties.setProperty(key, value);
  }

  public void remove(String key) {
    properties.remove(key);
  }

  public void clear() {
    properties.clear();
  }

  @Override
  public Properties props() {
    return properties;
  }

  @Override
  public int order() {
    return order;
  }
}
