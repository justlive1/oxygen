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

package vip.justlive.oxygen.jdbc.page;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * H2方言
 *
 * @author wubo
 */
public class H2PageDialect implements PageDialect {

  @Override
  public String page(Page<?> page, String sql) {
    return String.format("select * from (%s) tmp_pg limit %s offset %s", sql, page.getPageSize(),
        page.getOffset());
  }

  @Override
  public boolean supported(DatabaseMetaData meta) throws SQLException {
    String dbName = meta.getDatabaseProductName();
    switch (dbName) {
      case "H2":
      case "PostgreSQL":
      case "EnterpriseDB":
      case "HDB":
        return true;
      default:
    }
    return dbName.toLowerCase().contains("sqlite");
  }
}
