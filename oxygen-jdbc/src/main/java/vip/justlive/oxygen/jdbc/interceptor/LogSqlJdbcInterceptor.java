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
package vip.justlive.oxygen.jdbc.interceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * logSql
 *
 * @author wubo
 */
@Slf4j
public class LogSqlJdbcInterceptor implements JdbcInterceptor {

  @Override
  public int order() {
    return Integer.MAX_VALUE;
  }

  @Override
  public void before(SqlCtx ctx) {
    if (log.isDebugEnabled()) {
      log.debug("execute sql: {} -> params: {}", ctx.getSql(), ctx.getParams());
    }
  }
}
