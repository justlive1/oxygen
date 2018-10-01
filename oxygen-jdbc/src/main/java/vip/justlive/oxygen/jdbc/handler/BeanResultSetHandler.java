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
package vip.justlive.oxygen.jdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import vip.justlive.oxygen.jdbc.JdbcException;

/**
 * bean处理器
 *
 * @author wubo
 */
public class BeanResultSetHandler<T> implements ResultSetHandler<T> {

  private final Class<T> clazz;

  public BeanResultSetHandler(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public T handle(ResultSet rs) {
    try {
      return rs.next() ? BasicRowHandler.singleton().toBean(rs, clazz) : null;
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }
}
