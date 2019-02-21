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

import java.io.IOException;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * properties配置文件加载器
 * <br>
 * 支持classpath下配置文件，例如 classpath:/config/dev.properties,classpath*:/config/*.properties
 * <br>
 * 支持文件系统下配置文件，例如 file:/home/dev.properties, file:D:/conf/dev.properties<br> 支持配置文件中使用${k1:key}
 *
 * @author wubo
 */
@Slf4j
public class PropertiesLoader extends AbstractResourceLoader implements PropertySource {

  /**
   * 配置路径
   */
  private String[] locations;

  private Properties props = new Properties();

  /**
   * 使用路径创建{@code PropertiesLoader}
   *
   * @param locations 路径
   */
  public PropertiesLoader(String... locations) {
    this(ClassUtils.getDefaultClassLoader(), locations);
  }

  /**
   * 使用路径创建{@code PropertiesLoader}
   *
   * @param loader 类加载器
   * @param locations 路径
   */
  public PropertiesLoader(ClassLoader loader, String... locations) {
    this.locations = locations;
    this.loader = loader;
  }

  /**
   * 获取属性值
   *
   * @return 属性集合
   */
  @Override
  public Properties props() {
    if (!this.ready) {
      this.init();
    }
    return this.props;
  }

  @Override
  public void init() {
    if (this.ready) {
      return;
    }
    this.ready = true;
    this.resources.addAll(this.parse(this.locations));
    for (SourceResource resource : this.resources) {
      try {
        if (log.isDebugEnabled()) {
          log.debug("loading resource [{}]", resource.path());
        }
        props.load(this.getReader(resource));
      } catch (IOException e) {
        if (log.isDebugEnabled()) {
          log.debug("resource [{}] read error", resource.path(), e);
        }
        if (!ignoreNotFound) {
          throw Exceptions.wrap(e);
        }
      }
    }
  }

}
