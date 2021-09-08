/*
 * Copyright (C) 2021 the original author or authors.
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
 * string 处理
 *
 * @author wubo
 */
public class StringResultHandler implements ResultSetHandler<String> {

  @Override
  public String handle(ResultSet rs) {
    try {
      if (rs.next()) {
        Object value = rs.getObject(1);
        if (value != null) {
          return rs.getString(1);
        }
      }
      return null;
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }
}
