/*
 * Copyright (C) 2024 the original author or authors.
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
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.jdbc.record.OrderBy;

/**
 * order 排序插件
 *
 * @author wubo
 */
@Slf4j
public class OrderJdbcInterceptor implements JdbcInterceptor {

  public static final OrderJdbcInterceptor ORDER_JDBC_INTERCEPTOR = new OrderJdbcInterceptor();

  @Override
  public int order() {
    return PageJdbcInterceptor.PAGE_JDBC_INTERCEPTOR.order() - 10;
  }

  @Override
  public void before(SqlCtx ctx) {
    OrderBy orderBy = OrderBy.get();
    if (orderBy == null) {
      return;
    }
    if (log.isDebugEnabled()) {
      log.debug("start order by: {}", orderBy);
    }
    StringBuilder sb = new StringBuilder(ctx.getSql());
    sb.append(" order by ");
    for (int index = 0, len = orderBy.getOrders().size(); index < len; index++) {
      OrderBy.Order order = orderBy.getOrders().get(index);
      sb.append(String.join(Strings.COMMA, order.getColumns()));
      if (order.isDesc()) {
        sb.append(" desc");
      } else {
        sb.append(" asc");
      }
      if (index != len - 1) {
        sb.append(Strings.COMMA);
      }
    }
    ctx.setSql(sb.toString());
  }

  @Override
  public void after(SqlCtx ctx, Object result) {
    OrderBy.clear();
  }
}
