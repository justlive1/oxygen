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

package vip.justlive.oxygen.jdbc.record;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import vip.justlive.oxygen.jdbc.JdbcException;

@Getter
@RequiredArgsConstructor
public class OrderBy {

  private static final ThreadLocal<OrderBy> LOCAL = new ThreadLocal<>();

  private final Entity<?> entity;

  private List<Order> orders = new ArrayList<>();

  public OrderBy() {
    this(null);
  }

  @Data
  @Accessors(chain = true)
  public static class Order {

    private boolean desc;
    private List<String> columns = new ArrayList<>();

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      if (desc) {
        sb.append("desc ");
      } else {
        sb.append("asc ");
      }
      sb.append(columns);
      return sb.toString();
    }
  }

  public OrderBy asc(String... columns) {
    if (columns == null) {
      return this;
    }
    Order order = new Order().setDesc(false);
    for (String column : columns) {
      if (entity != null && !entity.getColumnKeys().contains(column)) {
        throw new JdbcException(String.format("order by [%s] not in columns", column));
      }
      order.getColumns().add(column);
    }
    orders.add(order);
    return this;
  }

  public OrderBy desc(String... columns) {
    if (columns == null) {
      return this;
    }
    Order order = new Order().setDesc(true);
    for (String column : columns) {
      if (entity != null && !entity.getColumnKeys().contains(column)) {
        throw new JdbcException(String.format("order by [%s] not in columns", column));
      }
      order.getColumns().add(column);
    }
    orders.add(order);
    return this;
  }

  public void start() {
    LOCAL.set(this);
  }

  @Override
  public String toString() {
    return "OrderBy(" + orders + ")";
  }

  public static OrderBy get() {
    return LOCAL.get();
  }

  public static void clear() {
    LOCAL.remove();
  }
}
