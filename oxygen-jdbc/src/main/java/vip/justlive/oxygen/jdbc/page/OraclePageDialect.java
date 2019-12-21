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
 * oracle 方言
 *
 * @author wubo
 */
public class OraclePageDialect implements PageDialect {

  @Override
  public String page(Page<?> page, String sql) {
    return String.format(
        "select * from (select row_.*, rownum rownum_ from (%s) row_ where rownum <= %s) where rownum_ > %s",
        sql, page.getPageSize() * page.getPageIndex(), page.getOffset());
  }

  @Override
  public boolean supported(DatabaseMetaData meta) throws SQLException {
    return "Oracle".equalsIgnoreCase(meta.getDatabaseProductName());
  }
}
