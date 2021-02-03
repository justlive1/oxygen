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

package vip.justlive.oxygen.jdbc.page;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * 分页方言
 *
 * @author wubo
 */
public interface PageDialect {

  /**
   * 构造分页sql
   *
   * @param page 分页参数
   * @param sql 原始sql
   * @return 分页sql
   */
  String page(Page<?> page, String sql);

  /**
   * 当前方言是否支持
   *
   * @param meta 数据库信息
   * @return is support
   * @throws SQLException sql异常
   */
  boolean supported(DatabaseMetaData meta) throws SQLException;
}
