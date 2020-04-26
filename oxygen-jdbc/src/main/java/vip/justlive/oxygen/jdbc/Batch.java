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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批处理
 *
 * @author wubo
 */

public class Batch {

  private final String dataSourceName;
  private final Map<String, PreparedStatement> psts;
  private Statement statement;


  private Batch(String dataSourceName) {
    this.dataSourceName = dataSourceName;
    this.psts = new HashMap<>(2, 1);
  }

  /**
   * 开启batch
   *
   * @return batch
   */
  public static Batch use() {
    return use(Jdbc.currentUse());
  }

  /**
   * 开启指定数据源batch
   *
   * @param dataSourceName 数据源名称
   * @return batch
   */
  public static Batch use(String dataSourceName) {
    Batch batch = new Batch(dataSourceName);
    Jdbc.startTx(dataSourceName);
    return batch;
  }

  /**
   * 添加sql
   *
   * @param sql sql
   * @return batch
   */
  public Batch addBatch(String sql) {
    try {
      if (statement == null) {
        statement = Jdbc.getConnection(dataSourceName).createStatement();
      }
      statement.addBatch(sql);
      return this;
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }


  /**
   * 添加批处理
   *
   * @param sql sql
   * @param params 参数
   * @return batch
   */
  public Batch addBatch(String sql, Object... params) {
    return addBatch(sql, Arrays.asList(params));
  }

  /**
   * 添加批处理
   *
   * @param sql sql
   * @param params 参数
   * @return batch
   */
  public synchronized Batch addBatch(String sql, List<Object> params) {
    try {
      PreparedStatement ps = psts.get(sql);
      if (ps == null) {
        ps = Jdbc.getConnection(dataSourceName).prepareStatement(sql);
        psts.put(sql, ps);
      }
      Jdbc.fillStatement(ps, params);
      ps.addBatch();
      return this;
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }

  /**
   * 提交
   */
  public void commit() {
    try {
      if (statement != null) {
        statement.executeBatch();
        Jdbc.close(statement);
      }
      if (!psts.isEmpty()) {
        for (PreparedStatement ps : psts.values()) {
          ps.executeBatch();
          ps.close();
        }
      }
      Jdbc.closeTx(dataSourceName);
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }

}
