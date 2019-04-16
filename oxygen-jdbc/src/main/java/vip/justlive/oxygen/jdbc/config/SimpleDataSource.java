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
package vip.justlive.oxygen.jdbc.config;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * 简单的数据源实现
 *
 * @author wubo
 */
public class SimpleDataSource implements DataSource {

  private final DataSourceConf conf;

  public SimpleDataSource(DataSourceConf conf) {
    this.conf = conf;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(conf.getUrl(), conf.getUsername(), conf.getPassword());
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return DriverManager.getConnection(conf.getUrl(), username, password);
  }

  @Override
  public <T> T unwrap(Class<T> clazz) {
    throw new UnsupportedOperationException(getClass().getName() + " is not a wrapper.");
  }

  @Override
  public boolean isWrapperFor(Class<?> clazz) {
    return false;
  }

  @Override
  public PrintWriter getLogWriter() {
    return DriverManager.getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) {
    DriverManager.setLogWriter(out);
  }

  @Override
  public int getLoginTimeout() {
    return DriverManager.getLoginTimeout();
  }

  @Override
  public void setLoginTimeout(int seconds) {
    DriverManager.setLoginTimeout(seconds);
  }

  @Override
  public Logger getParentLogger() {
    return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  }
}
