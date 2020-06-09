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
package vip.justlive.oxygen.jdbc.config;

import lombok.Data;
import vip.justlive.oxygen.core.util.base.MoreObjects;

/**
 * 数据源配置
 *
 * @author wubo
 */
@Data
@vip.justlive.oxygen.core.config.ValueConfig("oxygen.datasource")
public class DataSourceConf {

  /**
   * 是否禁用jdbc插件自动初始化
   */
  private boolean disabled = false;
  /**
   * 打印sql
   */
  private boolean logSql = false;
  /**
   * 是否为主数据源
   */
  private boolean primary = false;
  /**
   * 多数据源名称
   */
  private String[] multi;
  /**
   * 驱动
   */
  private String driverClassName;
  /**
   * 连接串
   */
  private String url;
  /**
   * 用户名
   */
  private String username;
  /**
   * 密码
   */
  private String password;
  /**
   * 数据源实现
   */
  private String type;
  /**
   * 数据源代称
   */
  private String alias;

  /**
   * 校验合法
   *
   * @return conf
   */
  public DataSourceConf validate() {
    MoreObjects
        .notNull(driverClassName, "driverClassName cannot be null");
    MoreObjects.notNull(url, "url cannot be null");
    MoreObjects.notNull(username, "username cannot be null");
    return this;
  }

  /**
   * 数据源类型
   */
  public enum TYPE {
    /**
     * hikari
     */
    HIKARI
  }

}
