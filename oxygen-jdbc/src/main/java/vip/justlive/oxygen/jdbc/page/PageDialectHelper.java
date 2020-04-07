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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import javax.sql.DataSource;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.jdbc.Jdbc;
import vip.justlive.oxygen.jdbc.JdbcException;

/**
 * 分页方言辅助类
 *
 * @author wubo
 */
@UtilityClass
public class PageDialectHelper {

  private static final Map<String, PageDialect> CACHE = new HashMap<>(4);
  private static final List<PageDialect> DIALECTS = new ArrayList<>(4);

  public static void clear() {
    CACHE.clear();
    DIALECTS.clear();
  }

  public static PageDialect get() {
    return CACHE.getOrDefault(Jdbc.currentUse(), UnknownPageDialect.DIALECT);
  }

  public static PageDialect guess(String name, DataSource dataSource) {
    if (DIALECTS.isEmpty()) {
      for (PageDialect dialect : ServiceLoader.load(PageDialect.class)) {
        DIALECTS.add(dialect);
      }
    }
    PageDialect dialect = CACHE.get(name);
    if (dialect != null) {
      return dialect;
    }
    dialect = guess(dataSource);
    CACHE.put(name, dialect);
    return dialect;
  }

  public static PageDialect guess(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {
      return guess(connection);
    } catch (Exception e) {
      throw JdbcException.wrap(e);
    }
  }

  public static PageDialect guess(Connection connection) {
    try {
      return guess(connection.getMetaData());
    } catch (Exception e) {
      throw JdbcException.wrap(e);
    }
  }

  public static PageDialect guess(DatabaseMetaData meta) {
    for (PageDialect dialect : DIALECTS) {
      try {
        if (dialect.supported(meta)) {
          return dialect;
        }
      } catch (SQLException e) {
        // ignore
      }
    }
    return UnknownPageDialect.DIALECT;
  }
}
