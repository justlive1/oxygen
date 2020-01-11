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
package vip.justlive.oxygen.jdbc.record;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import vip.justlive.oxygen.jdbc.Batch;
import vip.justlive.oxygen.jdbc.Jdbc;
import vip.justlive.oxygen.jdbc.JdbcException;
import vip.justlive.oxygen.jdbc.handler.ResultSetHandler;
import vip.justlive.oxygen.jdbc.page.Page;

/**
 * record utils
 *
 * @author wubo
 */
public final class Record {

  private static final Map<Class<?>, Model> CACHED = new ConcurrentHashMap<>(4);
  private static final String SEAT = " ? ";
  private static final String PARAM_STR = " %s = ? ";
  private static final String IN_PARAM_STR = "%s in (%s) ";
  private static final String COMMA = ",";
  private static final String AND = " and ";
  private static final String SET = " set ";
  private static final String INSERT = " insert into %s (%s) values (%s) ";
  private static final String UPDATE = " update ";
  private static final String WHERE = " where 1 = 1 ";
  private static final String DELETE = "delete from ";
  private static final String SELECT = " select * from %s" + WHERE;
  private static final String COUNT = "select count(*) from ";

  Record() {
  }

  /**
   * 根据主键获取record
   *
   * @param clazz 类
   * @param id 主键
   * @param <T> 泛型
   * @return 记录
   */
  public static <T> T findById(Class<T> clazz, Object id) {
    Model model = parseClass(clazz);
    String sql = model.baseQuery + AND + String.format(PARAM_STR, model.primary.name);
    return Jdbc.query(sql, clazz, id);
  }

  /**
   * 根据主键获取records
   *
   * @param clazz 类
   * @param ids 主键
   * @param <T> 泛型
   * @return 记录
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> findByIds(Class<T> clazz, List<?> ids) {
    Model model = parseClass(clazz);
    List<String> list = new ArrayList<>(ids.size());
    ids.forEach(id -> list.add(SEAT));
    String sql = model.baseQuery + AND + String
        .format(IN_PARAM_STR, model.primary.name, String.join(COMMA, list));
    return Jdbc.queryForList(sql, clazz, (List<Object>) ids);
  }

  /**
   * 根据record属性值获取集合
   *
   * @param obj record
   * @param <T> 泛型
   * @return list
   */
  public static <T> List<T> find(T obj) {
    return page(obj, null);
  }

  /**
   * 根据record属性值获取一个值
   *
   * @param obj record
   * @param <T> 泛型
   * @return list
   */
  public static <T> T findOne(T obj) {
    return findOne(obj, true);
  }

  /**
   * 根据record属性值获取一个值
   *
   * @param obj record
   * @param <T> 泛型
   * @return list
   */
  public static <T> T findOne(T obj, boolean throwEx) {
    List<T> list = find(obj);
    int size = list.size();
    if (size == 0) {
      return null;
    } else {
      if (size > 1 && throwEx) {
        throw new JdbcException("expect one but found " + size);
      }
      return list.get(0);
    }
  }

  /**
   * 获取所有集合
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return list
   */
  public static <T> List<T> findAll(Class<T> clazz) {
    return Jdbc.queryForList(parseClass(clazz).baseQuery, clazz);
  }

  /**
   * 获取分页
   *
   * @param obj record
   * @param page 分页参数
   * @param <T> 泛型
   * @return list
   */
  public static <T> List<T> page(T obj, Page<T> page) {
    @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) obj.getClass();
    Model model = parseClass(clazz);
    List<Object> params = new LinkedList<>();
    StringBuilder sb = new StringBuilder(model.baseQuery);
    margeWhere(model, obj, sb, params);
    if (page != null) {
      params.add(page);
    }
    return Jdbc.queryForList(sb.toString(), clazz, params);
  }

  /**
   * count
   *
   * @param obj record
   * @param <T> 泛型
   * @return count
   */
  public static <T> int count(T obj) {
    Model model = parseClass(obj.getClass());
    StringBuilder sb = new StringBuilder(COUNT).append(model.table).append(WHERE);
    List<Object> params = new LinkedList<>();
    margeWhere(model, obj, sb, params);
    return Jdbc.query(sb.toString(), ResultSetHandler.intHandler(), params);
  }

  /**
   * 修改record
   *
   * @param obj record
   * @param <T> 泛型
   * @return updated
   */
  public static <T> int update(T obj) {
    Model model = parseClass(obj.getClass());
    List<Object> params = new LinkedList<>();
    try {
      StringBuilder sb = new StringBuilder(UPDATE).append(model.table).append(SET);
      for (Property property : model.properties) {
        Object value = property.field.get(obj);
        if (!property.pk && value != null) {
          sb.append(String.format(PARAM_STR, property.name)).append(COMMA);
          params.add(value);
        }
      }
      sb.deleteCharAt(sb.length() - 1);
      params.add(model.primary.field.get(obj));
      sb.append(WHERE).append(AND).append(String.format(PARAM_STR, model.primary.name));
      return Jdbc.update(sb.toString(), params);
    } catch (IllegalAccessException e) {
      throw JdbcException.wrap(e);
    }
  }

  /**
   * 插入record
   *
   * @param obj record
   * @param <T> 泛型
   * @return updated
   */
  public static <T> int insert(T obj) {
    Model model = parseClass(obj.getClass());
    List<Object> params = new LinkedList<>();
    try {
      String sql = formatInsertSql(model, obj, params);
      return Jdbc.update(sql, params);
    } catch (IllegalAccessException e) {
      throw JdbcException.wrap(e);
    }
  }

  /**
   * 批量插入
   *
   * @param list 集合
   * @param <T> 泛型
   */
  public static <T> void insertBatch(List<T> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    Model model = parseClass(list.get(0).getClass());
    try {
      Batch batch = Batch.use();
      for (T obj : list) {
        List<Object> params = new LinkedList<>();
        String sql = formatInsertSql(model, obj, params);
        batch.addBatch(sql, params);
      }
      batch.commit();
    } catch (IllegalAccessException e) {
      throw JdbcException.wrap(e);
    }
  }

  /**
   * 根据id删除record
   *
   * @param clazz record
   * @param id 主键
   * @param <T> 泛型
   * @return updated
   */
  public static <T> int deleteById(Class<T> clazz, Object id) {
    Model model = parseClass(clazz);
    StringBuilder sb = new StringBuilder(DELETE).append(model.table).append(WHERE).append(AND)
        .append(String.format(PARAM_STR, model.primary.name));
    return Jdbc.update(sb.toString(), id);
  }

  /**
   * 根据record属性删除
   *
   * @param obj record
   * @param <T> 泛型
   * @return updated
   */
  public static <T> int delete(T obj) {
    Model model = parseClass(obj.getClass());
    StringBuilder sb = new StringBuilder(DELETE).append(model.table).append(WHERE);
    List<Object> params = new LinkedList<>();
    margeWhere(model, obj, sb, params);
    return Jdbc.update(sb.toString(), params);
  }

  private static String formatInsertSql(Model model, Object obj, List<Object> params)
      throws IllegalAccessException {
    StringBuilder fields = new StringBuilder();
    StringBuilder values = new StringBuilder();
    for (Property property : model.properties) {
      Object value = property.field.get(obj);
      if (value != null) {
        fields.append(property.name).append(COMMA);
        values.append(SEAT).append(COMMA);
        params.add(value);
      }
    }
    fields.deleteCharAt(fields.length() - 1);
    values.deleteCharAt(values.length() - 1);
    return String.format(INSERT, model.table, fields.toString(), values.toString());
  }

  private static void margeWhere(Model model, Object obj, StringBuilder sb, List<Object> params) {
    try {
      for (Property property : model.properties) {
        Object value = property.field.get(obj);
        if (value != null) {
          sb.append(AND).append(String.format(PARAM_STR, property.name));
          params.add(value);
        }
      }
    } catch (IllegalAccessException e) {
      throw JdbcException.wrap(e);
    }
  }

  private static Model parseClass(Class<?> clazz) {
    Model model = CACHED.get(clazz);
    if (model != null) {
      return model;
    }
    Table table = clazz.getAnnotation(Table.class);
    if (table == null) {
      throw new IllegalArgumentException(String.format("@Table is absent in class [%s]", clazz));
    }
    model = new Model();
    model.table = table.value();
    model.properties = new LinkedList<>();
    if (model.table.length() == 0) {
      model.table = clazz.getSimpleName().toLowerCase();
    }
    parseColumn(clazz, model);
    if (model.properties.isEmpty()) {
      throw new IllegalArgumentException(String.format("No @Column is found in class [%s]", clazz));
    }
    model.baseQuery = String.format(SELECT, model.table);
    CACHED.putIfAbsent(clazz, model);
    return CACHED.get(clazz);
  }

  private static void parseColumn(Class<?> clazz, Model model) {
    for (Field field : clazz.getDeclaredFields()) {
      Column column = field.getAnnotation(Column.class);
      if (column == null) {
        continue;
      }
      Property property = new Property();
      property.name = column.value();
      if (property.name.length() == 0) {
        property.name = field.getName().toLowerCase();
      }
      field.setAccessible(true);
      property.field = field;
      property.pk = column.pk();
      property.type = field.getType();
      model.properties.add(property);
      if (property.pk) {
        model.primary = property;
      }
    }
  }

  static class Model {

    String table;
    Property primary;
    List<Property> properties;
    String baseQuery;

  }

  static class Property {

    String name;
    Class<?> type;
    Field field;
    boolean pk;

  }
}
