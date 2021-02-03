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
package vip.justlive.oxygen.jdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * short 列处理器
 *
 * @author wubo
 */
public class ShortColumnHandler implements ColumnHandler {

  @Override
  public boolean supported(Class<?> type) {
    return type == short.class || type == Short.class;
  }

  @Override
  public Object fetch(ResultSet rs, int index) throws SQLException {
    return rs.getShort(index);
  }
}
