/*
 * Copyright (C) 2021 the original author or authors.
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import vip.justlive.oxygen.jdbc.Batch;
import vip.justlive.oxygen.jdbc.Jdbc;
import vip.justlive.oxygen.jdbc.JdbcException;
import vip.justlive.oxygen.jdbc.handler.ResultSetHandler;
import vip.justlive.oxygen.jdbc.page.Page;

/**
 * model
 *
 * @author wubo
 */
@Data
public class Entity<T> {

  private static final Map<Class<?>, Entity<?>> CACHED = new ConcurrentHashMap<>(4);
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

  private Class<T> type;
  private String table;
  private Property primary;
  private List<Property> properties;
  private String baseQuery;

  @SuppressWarnings("unchecked")
  public static <T> Entity<T> parse(Class<T> clazz) {
    return (Entity<T>) CACHED.computeIfAbsent(clazz, k -> parseClass(clazz));
  }

  public static <T> Entity<T> parseClass(Class<T> clazz) {
    Table table = clazz.getAnnotation(Table.class);
    if (table == null) {
      throw new IllegalArgumentException(String.format("@Table is absent in class [%s]", clazz));
    }
    Entity<T> entity = new Entity<>();
    entity.type = clazz;
    entity.table = table.value();
    entity.properties = new LinkedList<>();
    if (entity.table.length() == 0) {
      entity.table = clazz.getSimpleName().toLowerCase();
    }
    parseColumn(clazz, entity);
    if (entity.properties.isEmpty()) {
      throw new IllegalArgumentException(String.format("No @Column is found in class [%s]", clazz));
    }
    entity.baseQuery = String.format(SELECT, entity.table);
    return entity;
  }


  /**
   * 根据主键获取record
   *
   * @param id 主键
   * @return 记录
   */
  public T findById(Object id) {
    return findById(Jdbc.currentUse(), id);
  }

  /**
   * 根据主键获取records
   *
   * @param ids 主键
   * @return 记录
   */
  public List<T> findByIds(List<?> ids) {
    return findByIds(Jdbc.currentUse(), ids);
  }

  /**
   * 根据record属性值获取集合
   *
   * @param obj record
   * @return list
   */
  public List<T> find(T obj) {
    return find(Jdbc.currentUse(), obj);
  }

  /**
   * 根据record属性值获取一个值
   *
   * @param obj record
   * @return list
   */
  public T findOne(T obj) {
    return findOne(Jdbc.currentUse(), obj);
  }

  /**
   * 根据record属性值获取一个值
   *
   * @param obj     record
   * @param throwEx 是否抛出异常
   * @return list
   */
  public T findOne(T obj, boolean throwEx) {
    return findOne(Jdbc.currentUse(), obj, throwEx);
  }

  /**
   * 获取所有集合
   *
   * @return list
   */
  public List<T> findAll() {
    return findAll(Jdbc.currentUse());
  }

  /**
   * 获取分页
   *
   * @param obj  record
   * @param page 分页参数
   * @return list
   */
  public List<T> page(T obj, Page<T> page) {
    return page(Jdbc.currentUse(), obj, page);
  }

  /**
   * count
   *
   * @param obj record
   * @return count
   */
  public int count(T obj) {
    return count(Jdbc.currentUse(), obj);
  }

  /**
   * 修改record
   *
   * @param obj record
   * @return updated
   */
  public int updateById(T obj) {
    return updateById(Jdbc.currentUse(), obj);
  }

  /**
   * 插入record
   *
   * @param obj record
   * @return updated
   */
  public int insert(T obj) {
    return insert(Jdbc.currentUse(), obj);
  }

  /**
   * 批量插入
   *
   * @param list 集合
   */
  public void insertBatch(List<T> list) {
    insertBatch(Jdbc.currentUse(), list);
  }

  /**
   * 根据id删除record
   *
   * @param id 主键
   * @return updated
   */
  public int deleteById(Object id) {
    return deleteById(Jdbc.currentUse(), id);
  }

  /**
   * 根据ids删除record
   *
   * @param ids 注解列表
   * @return updated
   */
  public int deleteByIds(List<?> ids) {
    return deleteByIds(Jdbc.currentUse(), ids);
  }

  /**
   * 根据record属性删除
   *
   * @param obj record
   * @return updated
   */
  public int delete(T obj) {
    return delete(Jdbc.currentUse(), obj);
  }

  /**
   * 根据主键获取record
   *
   * @param dataSourceName 数据源名称
   * @param id             主键
   * @return 记录
   */
  public T findById(String dataSourceName, Object id) {
    return findById(Jdbc.getConnection(dataSourceName), id);
  }

  /**
   * 根据主键获取records
   *
   * @param dataSourceName 数据源名称
   * @param ids            主键
   * @return 记录
   */
  public List<T> findByIds(String dataSourceName, List<?> ids) {
    return findByIds(Jdbc.getConnection(dataSourceName), ids);
  }

  /**
   * 根据record属性值获取集合
   *
   * @param dataSourceName 数据源名称
   * @param obj            record
   * @return list
   */
  public List<T> find(String dataSourceName, T obj) {
    return find(Jdbc.getConnection(dataSourceName), obj);
  }

  /**
   * 根据record属性值获取一个值
   *
   * @param dataSourceName 数据源名称
   * @param obj            record
   * @return list
   */
  public T findOne(String dataSourceName, T obj) {
    return findOne(Jdbc.getConnection(dataSourceName), obj);
  }

  /**
   * 根据record属性值获取一个值
   *
   * @param dataSourceName 数据源名称
   * @param obj            record
   * @param throwEx        是否抛出异常
   * @return list
   */
  public T findOne(String dataSourceName, T obj, boolean throwEx) {
    return findOne(Jdbc.getConnection(dataSourceName), obj, throwEx);
  }

  /**
   * 获取所有集合
   *
   * @param dataSourceName 数据源名称
   * @return list
   */
  public List<T> findAll(String dataSourceName) {
    return findAll(Jdbc.getConnection(dataSourceName));
  }

  /**
   * 获取分页
   *
   * @param dataSourceName 数据源名称
   * @param obj            record
   * @param page           分页参数
   * @return list
   */
  public List<T> page(String dataSourceName, T obj, Page<T> page) {
    return page(Jdbc.getConnection(dataSourceName), obj, page);
  }

  /**
   * count
   *
   * @param dataSourceName 数据源名称
   * @param obj            record
   * @return count
   */
  public int count(String dataSourceName, T obj) {
    return count(Jdbc.getConnection(dataSourceName), obj);
  }

  /**
   * 修改record
   *
   * @param dataSourceName 数据源名称
   * @param obj            record
   * @return updated
   */
  public int updateById(String dataSourceName, T obj) {
    return updateById(Jdbc.getConnection(dataSourceName), obj);
  }

  /**
   * 插入record
   *
   * @param dataSourceName 数据源名称
   * @param obj            record
   * @return updated
   */
  public int insert(String dataSourceName, T obj) {
    return insert(Jdbc.getConnection(dataSourceName), obj);
  }

  /**
   * 批量插入
   *
   * @param dataSourceName 数据源名称
   * @param list           集合
   */
  public void insertBatch(String dataSourceName, List<T> list) {
    insertBatch(Jdbc.getConnection(dataSourceName), list);
  }

  /**
   * 根据id删除record
   *
   * @param dataSourceName 数据源名称
   * @param id             主键
   * @return updated
   */
  public int deleteById(String dataSourceName, Object id) {
    return deleteById(Jdbc.getConnection(dataSourceName), id);
  }

  /**
   * 根据ids删除record
   *
   * @param dataSourceName 数据源名称
   * @param ids            注解列表
   * @return updated
   */
  public int deleteByIds(String dataSourceName, List<?> ids) {
    return deleteByIds(Jdbc.getConnection(dataSourceName), ids);
  }

  /**
   * 根据record属性删除
   *
   * @param dataSourceName 数据源名称
   * @param obj            record
   * @return updated
   */
  public int delete(String dataSourceName, T obj) {
    return delete(Jdbc.getConnection(dataSourceName), obj);
  }

  /**
   * 根据主键获取record
   *
   * @param conn 数据库连接
   * @param id   主键
   * @return 记录
   */
  public T findById(Connection conn, Object id) {
    String sql = baseQuery + AND + String.format(PARAM_STR, primary.name);
    return Jdbc.query(conn, sql, ResultSetHandler.beanHandler(type), id);
  }

  /**
   * 根据主键获取records
   *
   * @param conn 数据库连接
   * @param ids  主键
   * @return 记录
   */
  @SuppressWarnings("unchecked")
  public List<T> findByIds(Connection conn, List<?> ids) {
    List<String> list = new ArrayList<>(ids.size());
    ids.forEach(id -> list.add(SEAT));
    String sql =
        baseQuery + AND + String.format(IN_PARAM_STR, primary.name, String.join(COMMA, list));
    return Jdbc.query(conn, sql, ResultSetHandler.beanListHandler(type), (List<Object>) ids);
  }

  /**
   * 根据record属性值获取集合
   *
   * @param conn 数据库连接
   * @param obj  record
   * @return list
   */
  public List<T> find(Connection conn, T obj) {
    return page(conn, obj, null);
  }

  /**
   * 根据record属性值获取一个值
   *
   * @param conn 数据库连接
   * @param obj  record
   * @return list
   */
  public T findOne(Connection conn, T obj) {
    return findOne(conn, obj, true);
  }

  /**
   * 根据record属性值获取一个值
   *
   * @param conn    数据库连接
   * @param obj     record
   * @param throwEx 是否抛出异常
   * @return list
   */
  public T findOne(Connection conn, T obj, boolean throwEx) {
    List<T> list = find(conn, obj);
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
   * @param conn 数据库连接
   * @return list
   */
  public List<T> findAll(Connection conn) {
    return Jdbc.query(conn, baseQuery, ResultSetHandler.beanListHandler(type));
  }

  /**
   * 获取分页
   *
   * @param conn 数据库连接
   * @param obj  record
   * @param page 分页参数
   * @return list
   */
  public List<T> page(Connection conn, T obj, Page<T> page) {
    List<Object> params = new LinkedList<>();
    StringBuilder sb = new StringBuilder(baseQuery);
    margeWhere(this, obj, sb, params);
    if (page != null) {
      params.add(page);
    }
    return Jdbc.query(conn, sb.toString(), ResultSetHandler.beanListHandler(type), params);
  }

  /**
   * count
   *
   * @param conn 数据库连接
   * @param obj  record
   * @return count
   */
  public int count(Connection conn, T obj) {
    StringBuilder sb = new StringBuilder(COUNT).append(table).append(WHERE);
    List<Object> params = new LinkedList<>();
    margeWhere(this, obj, sb, params);
    return Jdbc.query(conn, sb.toString(), ResultSetHandler.intHandler(), params);
  }

  /**
   * 修改record
   *
   * @param conn 数据库连接
   * @param obj  record
   * @return updated
   */
  public int updateById(Connection conn, T obj) {
    List<Object> params = new LinkedList<>();
    try {
      StringBuilder sb = new StringBuilder(UPDATE).append(table).append(SET);
      for (Property property : properties) {
        Object value = property.field.get(obj);
        if (!property.pk && value != null) {
          sb.append(String.format(PARAM_STR, property.name)).append(COMMA);
          params.add(value);
        }
      }
      sb.deleteCharAt(sb.length() - 1);
      params.add(primary.field.get(obj));
      sb.append(WHERE).append(AND).append(String.format(PARAM_STR, primary.name));
      return Jdbc.update(conn, sb.toString(), params);
    } catch (IllegalAccessException e) {
      throw JdbcException.wrap(e);
    }
  }

  /**
   * 插入record
   *
   * @param conn 数据库连接
   * @param obj  record
   * @return updated
   */
  public int insert(Connection conn, T obj) {
    List<Object> params = new LinkedList<>();
    try {
      String sql = formatInsertSql(this, obj, params);
      if (primary != null && canConvert(primary.type)) {
        int id = Jdbc.update(conn, sql, params, conn.getAutoCommit(), true);
        convert(obj, primary, id);
        return id;
      }
      return Jdbc.update(conn, sql, params);
    } catch (IllegalAccessException | SQLException e) {
      throw JdbcException.wrap(e);
    }
  }

  /**
   * 批量插入
   *
   * @param conn 数据库连接
   * @param list 集合
   */
  public void insertBatch(Connection conn, List<T> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    try {
      Batch batch = Batch.use(conn);
      for (T obj : list) {
        List<Object> params = new LinkedList<>();
        String sql = formatInsertSql(this, obj, params);
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
   * @param conn 数据库连接
   * @param id   主键
   * @return updated
   */
  public int deleteById(Connection conn, Object id) {
    String sql = DELETE + table + WHERE + AND + String.format(PARAM_STR, primary.name);
    return Jdbc.update(conn, sql, id);
  }

  /**
   * 根据ids删除record
   *
   * @param conn 数据库连接
   * @param ids  注解列表
   * @return updated
   */
  @SuppressWarnings("unchecked")
  public int deleteByIds(Connection conn, List<?> ids) {
    List<String> list = new ArrayList<>(ids.size());
    ids.forEach(id -> list.add(SEAT));
    String sql = DELETE + table + WHERE + AND + String
        .format(IN_PARAM_STR, primary.name, String.join(COMMA, list));
    return Jdbc.update(conn, sql, (List<Object>) ids);
  }

  /**
   * 根据record属性删除
   *
   * @param conn 数据库连接
   * @param obj  record
   * @return updated
   */
  public int delete(Connection conn, T obj) {
    StringBuilder sb = new StringBuilder(DELETE).append(table).append(WHERE);
    List<Object> params = new LinkedList<>();
    margeWhere(this, obj, sb, params);
    return Jdbc.update(conn, sb.toString(), params);
  }

  private static String formatInsertSql(Entity<?> entity, Object obj, List<Object> params)
      throws IllegalAccessException {
    StringBuilder fields = new StringBuilder();
    StringBuilder values = new StringBuilder();
    for (Property property : entity.properties) {
      Object value = property.field.get(obj);
      if (value != null) {
        fields.append(property.name).append(COMMA);
        values.append(SEAT).append(COMMA);
        params.add(value);
      }
    }
    fields.deleteCharAt(fields.length() - 1);
    values.deleteCharAt(values.length() - 1);
    return String.format(INSERT, entity.table, fields.toString(), values.toString());
  }

  private static void margeWhere(Entity<?> entity, Object obj, StringBuilder sb,
      List<Object> params) {
    try {
      for (Property property : entity.properties) {
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


  private static boolean canConvert(Class<?> type) {
    return type == int.class || type == long.class || type == Integer.class || type == Long.class;
  }

  private static void convert(Object obj, Property property, int id) throws IllegalAccessException {
    if (id == -1) {
      return;
    }
    Object value = id;
    if (property.type == long.class || property.type == Long.class) {
      value = (long) id;
    }
    property.field.setAccessible(true);
    property.field.set(obj, value);
  }

  private static void parseColumn(Class<?> clazz, Entity<?> entity) {
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
      entity.properties.add(property);
      if (property.pk) {
        entity.primary = property;
      }
    }
  }

  public static class Property {

    String name;
    Class<?> type;
    Field field;
    boolean pk;
  }
}
