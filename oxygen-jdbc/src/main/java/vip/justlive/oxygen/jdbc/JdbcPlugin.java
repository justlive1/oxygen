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
package vip.justlive.oxygen.jdbc;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.bean.Singleton;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.io.FirstResourceLoader;
import vip.justlive.oxygen.core.util.io.IoUtils;
import vip.justlive.oxygen.core.util.io.SourceResource;
import vip.justlive.oxygen.jdbc.config.DataSourceBuilder;
import vip.justlive.oxygen.jdbc.config.DataSourceConf;
import vip.justlive.oxygen.jdbc.interceptor.JdbcInterceptor;
import vip.justlive.oxygen.jdbc.interceptor.LogSqlJdbcInterceptor;

/**
 * jdbc操作
 *
 * @author wubo
 */
@Slf4j
public class JdbcPlugin implements Plugin {

  @Override
  public void start() {
    DataSourceConf primary = ConfigFactory.load(DataSourceConf.class);
    if (primary.isDisabled()) {
      log.info("Jdbc plugin is disabled");
      return;
    }
    if (primary.isLogSql()) {
      Jdbc.addJdbcInterceptor(new LogSqlJdbcInterceptor());
    }

    lookupDataSource();

    Singleton.getList(JdbcInterceptor.class).forEach(Jdbc::addJdbcInterceptor);
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 600;
  }

  @Override
  public void stop() {
    Jdbc.shutdown();
  }

  /**
   * 查找数据源
   */
  private void lookupDataSource() {
    loadFromConfig();
  }

  /**
   * 从配置文件中获取
   */
  private void loadFromConfig() {
    DataSourceConf primary = ConfigFactory.load(DataSourceConf.class);
    if (primary.getUrl() != null && primary.getUrl().length() > 0) {
      addDataSource(primary);
    }
    if (primary.getMulti() != null && primary.getMulti().length > 0) {
      for (String name : primary.getMulti()) {
        DataSourceConf conf = ConfigFactory
            .load(DataSourceConf.class, String.format(Jdbc.TEMPLATE, name));
        addDataSource(conf, name);
      }
    }
  }

  private void addDataSource(DataSourceConf conf) {
    DataSource ds = DataSourceBuilder.build(conf.validate());
    Jdbc.addPrimaryDataSource(ds);
    if (conf.getAlias() != null && conf.getAlias().length() > 0) {
      Jdbc.addDataSource(conf.getAlias(), ds);
    }
    if (conf.getInitScripts() != null) {
      runInitScripts(Jdbc.PRIMARY_KEY, conf.getInitScripts());
    }
  }

  private void addDataSource(DataSourceConf conf, String name) {
    DataSource ds = DataSourceBuilder.build(conf.validate(), name);
    Jdbc.addDataSource(name, ds);
    if (conf.isPrimary()) {
      Jdbc.addPrimaryDataSource(ds);
    }
    if (conf.getAlias() != null && conf.getAlias().length() > 0) {
      Jdbc.addDataSource(conf.getAlias(), ds);
    }
    if (conf.getInitScripts() != null) {
      runInitScripts(name, conf.getInitScripts());
    }
  }

  private void runInitScripts(String name, String[] scripts) {
    try {
      Batch batch = Batch.use(name);
      for (String script : scripts) {
        SourceResource resource = new FirstResourceLoader(script).getResource();
        if (resource == null) {
          log.warn("script not found: {}", script);
          continue;
        }
        for (String sql : IoUtils.toString(resource.getInputStream()).split(Strings.SEMICOLON)) {
          if (Strings.hasText(sql)) {
            batch.addBatch(sql);
          }
        }
      }
      batch.commit();
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }

}
