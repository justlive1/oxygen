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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.WeakHashMap;
import vip.justlive.oxygen.jdbc.JdbcException;
import vip.justlive.oxygen.jdbc.record.Column;

/**
 * 基本行处理器
 *
 * @author wubo
 */
public class BasicRowHandler implements RowHandler {

  private static final BasicRowHandler INSTANCE = new BasicRowHandler();
  private static final Iterable<ColumnHandler> COLUMN_HANDLERS = ServiceLoader
      .load(ColumnHandler.class);
  private static final Iterable<PropertyHandler> PROPERTY_HANDLERS = ServiceLoader
      .load(PropertyHandler.class);

  private final WeakHashMap<Class<?>, Map<String, Field>> cache = new WeakHashMap<>(4);

  /**
   * 获取单例
   *
   * @return basicRowHandler
   */
  public static BasicRowHandler singleton() {
    return INSTANCE;
  }

  @Override
  public Object[] toArray(ResultSet rs) {
    try {
      ResultSetMetaData meta = rs.getMetaData();
      int cols = meta.getColumnCount();
      Object[] result = new Object[cols];
      for (int i = 0; i < cols; i++) {
        result[i] = rs.getObject(i + 1);
      }
      return result;
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }

  @Override
  public Map<String, Object> toMap(ResultSet rs) {
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int cols = rsmd.getColumnCount();
      Map<String, Object> result = new HashMap<>(16);
      for (int i = 1; i <= cols; i++) {
        String columnName = getColumnName(rsmd, i);
        result.put(columnName, rs.getObject(i));
      }
      return result;
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }

  @Override
  public <T> T toBean(ResultSet rs, Class<T> type) {
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int cols = rsmd.getColumnCount();
      String[] columns = new String[cols + 1];
      for (int col = 1; col <= cols; col++) {
        columns[col] = getColumnName(rsmd, col).toLowerCase();
      }

      return fillBeanProperty(rs, type, columns);
    } catch (Exception e) {
      throw JdbcException.wrap(e);
    }
  }

  private <T> T fillBeanProperty(ResultSet rs, Class<T> type, String[] columns)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, SQLException {
    T bean = type.getConstructor().newInstance();
    Map<String, Field> fields = getBeanInfo(type);
    for (int i = 1; i < columns.length; i++) {
      Field field = fields.get(columns[i]);
      if (field == null) {
        continue;
      }
      Class<?> propType = field.getType();
      Object value = this.processColumn(rs, i, propType);
      if (value != null) {
        this.setValue(bean, field, value);
      }
    }
    return bean;
  }

  private Object processColumn(ResultSet rs, int index, Class<?> propType) throws SQLException {
    Object value = rs.getObject(index);
    if (value == null || value.getClass() == propType) {
      return value;
    }

    for (ColumnHandler columnHandler : COLUMN_HANDLERS) {
      if (columnHandler.supported(propType)) {
        return columnHandler.fetch(rs, index);
      }
    }

    return value;
  }

  private void setValue(Object target, Field field, Object value) throws IllegalAccessException {
    field.setAccessible(true);
    Class<?> type = field.getType();
    if (type.isInstance(value)) {
      field.set(target, value);
      return;
    }

    for (PropertyHandler handler : PROPERTY_HANDLERS) {
      if (handler.supported(type, value)) {
        value = handler.cast(type, value);
        break;
      }
    }
    field.set(target, value);
  }

  private String getColumnName(ResultSetMetaData rsmd, int index) throws SQLException {
    String columnName = rsmd.getColumnLabel(index);
    if (null == columnName || 0 == columnName.length()) {
      columnName = rsmd.getColumnName(index);
    }
    return columnName;
  }

  private Map<String, Field> getBeanInfo(Class<?> clazz) {
    Map<String, Field> fields = cache.get(clazz);
    if (fields != null) {
      return fields;
    }
    synchronized (this) {
      fields = new HashMap<>(4);
      Class<?> type = clazz;
      while (type != null && type != Object.class) {
        for (Field field : type.getDeclaredFields()) {
          String name = field.getName();
          Column column = field.getAnnotation(Column.class);
          if (column != null && column.value().length() > 0) {
            name = column.value();
          }
          fields.put(name, field);
        }
        type = type.getSuperclass();
      }
      cache.put(clazz, fields);
    }
    return fields;
  }

}
