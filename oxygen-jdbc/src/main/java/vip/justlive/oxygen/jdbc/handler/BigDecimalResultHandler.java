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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import vip.justlive.oxygen.jdbc.JdbcException;

/**
 * BigDecimal 处理
 * <br>
 * 适用于sum count 返回单列的sql
 *
 * @author wubo
 */
public class BigDecimalResultHandler implements ResultSetHandler<BigDecimal> {

  public static final BigDecimalResultHandler INSTANCE = new BigDecimalResultHandler();

  @Override
  public BigDecimal handle(ResultSet rs) {
    try {
      if (rs.next()) {
        Object value = rs.getObject(1);
        if (value != null) {
          return rs.getBigDecimal(1);
        }
      }
      return BigDecimal.ZERO;
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }
}
