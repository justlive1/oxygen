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
import java.util.ArrayList;
import java.util.List;
import vip.justlive.oxygen.jdbc.JdbcException;

/**
 * array List结果集转换器
 *
 * @author wubo
 */
public class ArrayListResultSetHandler implements ResultSetHandler<List<Object[]>> {

  public static final ArrayListResultSetHandler INSTANCE = new ArrayListResultSetHandler();

  @Override
  public List<Object[]> handle(ResultSet rs) {
    List<Object[]> result = new ArrayList<>();
    try {
      while (rs.next()) {
        result.add(BasicRowHandler.singleton().toArray(rs));
      }
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
    return result;
  }
}
