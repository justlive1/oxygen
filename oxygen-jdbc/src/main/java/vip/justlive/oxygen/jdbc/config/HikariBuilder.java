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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.jdbc.config.DataSourceConf.TYPE;

/**
 * Hikari数据源构造
 *
 * @author wubo
 */
public class HikariBuilder {

  private static final boolean ENABLED = ClassUtils.isPresent("com.zaxxer.hikari.HikariDataSource");

  HikariBuilder() {
  }

  /**
   * 是否可用
   *
   * @param conf 配置
   * @return true为可用
   */
  public static boolean isEnabled(DataSourceConf conf) {
    return ENABLED && (conf.getType() == null || conf.getType().length() == 0 || TYPE.HIKARI.name()
        .equalsIgnoreCase(conf.getType()));
  }

  /**
   * 构造DataSource
   *
   * @param conf 数据源配置
   * @return DataSource
   */
  public static DataSource build(DataSourceConf conf) {
    HikariConfig config = new HikariConfig();
    ConfigFactory.load(config, "datasource.hikari");
    marge(conf, config);
    return new HikariDataSource(config);
  }

  /**
   * 构造DataSource
   *
   * @param conf 数据源配置
   * @param name 数据源名称
   * @return DataSource
   */
  public static DataSource build(DataSourceConf conf, String name) {
    HikariConfig config = new HikariConfig();
    ConfigFactory.load(config, String.format("datasource.%s.hikari", name));
    marge(conf, config);
    return new HikariDataSource(config);
  }

  private static void marge(DataSourceConf conf, HikariConfig config) {
    config.setUsername(conf.getUsername());
    config.setPassword(conf.getPassword());
    config.setDriverClassName(conf.getDriverClassName());
    config.setJdbcUrl(conf.getUrl());
  }
}
