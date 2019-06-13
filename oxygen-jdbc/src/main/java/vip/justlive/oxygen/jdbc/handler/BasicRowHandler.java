/*
 * Copyright (C) 2018 justlive1
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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import vip.justlive.oxygen.jdbc.JdbcException;

/**
 * 基本行处理器
 *
 * @author wubo
 */
public class BasicRowHandler implements RowHandler {

  private static final BasicRowHandler INSTANCE = new BasicRowHandler();
  private static final int PROPERTY_NOT_FOUND = -1;
  private static final Iterable<ColumnHandler> COLUMN_HANDLERS = ServiceLoader
      .load(ColumnHandler.class);

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
      PropertyDescriptor[] props = Introspector.getBeanInfo(type).getPropertyDescriptors();
      ResultSetMetaData rsmd = rs.getMetaData();
      int cols = rsmd.getColumnCount();
      int[] propertyArray = new int[cols + 1];
      Arrays.fill(propertyArray, PROPERTY_NOT_FOUND);

      Map<String, Integer> indexMap = new HashMap<>(8);
      for (int col = 1; col <= cols; col++) {
        String columnName = getColumnName(rsmd, col);
        indexMap.put(columnName.toLowerCase(), col);
      }

      fillPropertyArray(propertyArray, indexMap, props);
      return fillBeanProperty(rs, type, props, propertyArray);
    } catch (Exception e) {
      throw JdbcException.wrap(e);
    }
  }

  private <T> T fillBeanProperty(ResultSet rs, Class<T> type, PropertyDescriptor[] props,
      int[] propertyArray)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, SQLException {
    T bean = type.getConstructor().newInstance();
    for (int i = 1; i < propertyArray.length; i++) {
      if (propertyArray[i] == PROPERTY_NOT_FOUND) {
        continue;
      }

      PropertyDescriptor prop = props[propertyArray[i]];
      Class<?> propType = prop.getPropertyType();

      Object value = null;
      if (propType != null) {
        value = this.processColumn(rs, i, propType);
      }
      if (value != null) {
        this.setValue(bean, prop, value);
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

  private void setValue(Object target, PropertyDescriptor prop, Object value)
      throws InvocationTargetException, IllegalAccessException {
    Method setter = prop.getWriteMethod();
    if (setter == null || setter.getParameterTypes().length != 1) {
      return;
    }
    setter.invoke(target, value);
  }

  private String getColumnName(ResultSetMetaData rsmd, int index) throws SQLException {
    String columnName = rsmd.getColumnLabel(index);
    if (null == columnName || 0 == columnName.length()) {
      columnName = rsmd.getColumnName(index);
    }
    return columnName;
  }


  private void fillPropertyArray(int[] propertyArray, Map<String, Integer> integerMap,
      PropertyDescriptor[] props) {
    for (int i = 0; i < props.length; i++) {
      String propertyName = props[i].getName();
      Integer index = integerMap.get(propertyName.toLowerCase());
      if (index != null) {
        propertyArray[index] = i;
      }
    }
  }
}
