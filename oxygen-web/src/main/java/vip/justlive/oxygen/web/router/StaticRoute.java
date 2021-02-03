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
package vip.justlive.oxygen.web.router;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 静态资源router
 *
 * @author wubo
 */
@Data
@Accessors(fluent = true)
public class StaticRoute {

  /**
   * 静态资源前缀
   */
  private String prefix;
  /**
   * 静态资源根目录
   */
  private List<String> locations = new LinkedList<>();
  /**
   * 缓存时间 单位秒
   */
  private int maxAge;

  /**
   * 是否启用缓存
   */
  private boolean cachingEnabled;

  StaticRoute() {
  }

  /**
   * 设置请求前缀
   *
   * @param prefix 前缀
   * @return route
   */
  public StaticRoute prefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  /**
   * 设置文件路径
   *
   * @param path 路径
   * @return route
   */
  public StaticRoute location(String path) {
    this.locations.add(path);
    return this;
  }

}
