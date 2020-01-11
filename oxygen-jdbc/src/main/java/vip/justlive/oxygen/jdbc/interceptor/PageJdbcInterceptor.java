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

import java.util.ArrayList;
import java.util.List;
import vip.justlive.oxygen.jdbc.Jdbc;
import vip.justlive.oxygen.jdbc.handler.ResultSetHandler;
import vip.justlive.oxygen.jdbc.page.Page;
import vip.justlive.oxygen.jdbc.page.PageDialectHelper;

/**
 * 分页组件
 *
 * @author wubo
 */
public class PageJdbcInterceptor implements JdbcInterceptor {

  public static final PageJdbcInterceptor PAGE_JDBC_INTERCEPTOR = new PageJdbcInterceptor();

  @Override
  public void before(SqlCtx ctx) {
    if (ctx.getParams() == null || ctx.getParams().isEmpty() || !(ctx.getParams()
        .get(ctx.getParams().size() - 1) instanceof Page)) {
      return;
    }

    Page<?> page = (Page<?>) ctx.getParams().get(ctx.getParams().size() - 1);
    ctx.setPaged(page.isHasPaged());
    if (page.isHasPaged()) {
      return;
    }

    page.setHasPaged(true);
    if (page.isSearchCount()) {
      Integer count = Jdbc.query(Jdbc.getConnection(Jdbc.currentUse()),
          String.format("select count(*) from (%s) tmp_ct", ctx.getSql()),
          ResultSetHandler.intHandler(), ctx.getParams(), false);
      page.setTotalNumber(count.longValue());
    }
    ctx.setSql(PageDialectHelper.get().page(page, ctx.getSql()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void after(SqlCtx ctx, Object result) {
    if (ctx.isPaged() || ctx.getParams() == null || ctx.getParams().isEmpty() || !(ctx.getParams()
        .get(ctx.getParams().size() - 1) instanceof Page)) {
      return;
    }

    List<?> list;
    if (result != null) {
      list = (List<?>) result;
    } else {
      list = new ArrayList<>(0);
    }
    ((Page) ctx.getParams().get(ctx.getParams().size() - 1)).setItems(list);
  }
}
