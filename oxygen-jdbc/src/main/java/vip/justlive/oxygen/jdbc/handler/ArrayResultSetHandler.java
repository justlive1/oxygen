/*
 * Copyright (C) 2019 the original author or authors.
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
package vip.justlive.oxygen.jdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import vip.justlive.oxygen.jdbc.JdbcException;

/**
 * 数组结果集处理器
 *
 * @author wubo
 */
public class ArrayResultSetHandler implements ResultSetHandler<Object[]> {

  public static final ArrayResultSetHandler INSTANCE = new ArrayResultSetHandler();
  private static final Object[] EMPTY = new Object[0];

  @Override
  public Object[] handle(ResultSet rs) {
    try {
      return rs.next() ? BasicRowHandler.singleton().toArray(rs) : EMPTY;
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }
}
