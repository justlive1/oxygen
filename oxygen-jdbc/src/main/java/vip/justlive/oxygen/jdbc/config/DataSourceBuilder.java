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

import javax.sql.DataSource;

/**
 * dataSource构造器
 *
 * @author wubo
 */
public class DataSourceBuilder {

  DataSourceBuilder() {
  }

  /**
   * 构造数据源
   *
   * @param conf 数据源配置
   * @return DataSource
   */
  public static DataSource build(DataSourceConf conf) {
    if (HikariBuilder.isEnabled(conf)) {
      return HikariBuilder.build(conf);
    }
    return new SimpleDataSource(conf);
  }

  /**
   * 构造数据源
   *
   * @param conf 数据源配置
   * @param name 数据源名称
   * @return DataSource
   */
  public static DataSource build(DataSourceConf conf, String name) {
    if (HikariBuilder.isEnabled(conf)) {
      return HikariBuilder.build(conf, name);
    }
    return new SimpleDataSource(conf);
  }

}
