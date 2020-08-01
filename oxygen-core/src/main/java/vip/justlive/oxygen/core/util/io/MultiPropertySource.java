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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import vip.justlive.oxygen.core.util.base.PlaceHolderHelper;

/**
 * 多重组合配置属性源
 *
 * @author wubo
 */
public class MultiPropertySource implements PropertySource {

  private final List<PropertySource> sources = new ArrayList<>();

  public MultiPropertySource addSource(PropertySource source) {
    if (!this.sources.contains(source)) {
      this.sources.add(source);
      Collections.sort(sources);
    }
    return this;
  }

  public MultiPropertySource clear() {
    sources.clear();
    return this;
  }

  @Override
  public Properties props() {
    Properties props = new Properties();
    for (int i = sources.size() - 1; i >= 0; i--) {
      props.putAll(sources.get(i).props());
    }
    return props;
  }

  @Override
  public String getProperty(String key) {
    for (PropertySource source : sources) {
      String value = source.props().getProperty(key);
      if (value != null) {
        return getPlaceholderProperty(value);
      }
    }
    return null;
  }

  @Override
  public String getPlaceholderProperty(String placeholder) {
    return PlaceHolderHelper.DEFAULT_HELPER.replacePlaceholders(placeholder, this::getRawProperty);
  }

  @Override
  public boolean containPrefix(String prefix) {
    for (PropertySource source : sources) {
      if (source.containPrefix(prefix)) {
        return true;
      }
    }
    return false;
  }
}
