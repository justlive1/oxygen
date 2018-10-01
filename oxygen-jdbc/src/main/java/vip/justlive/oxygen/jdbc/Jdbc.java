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
package vip.justlive.oxygen.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import vip.justlive.oxygen.jdbc.handler.ResultSetHandler;
import vip.justlive.oxygen.jdbc.interceptor.JdbcInterceptor;

/**
 * jdbc操作类
 *
 * @author wubo
 */
public class Jdbc {

  static final String TEMPLATE = "datasource.%s";
  static final String PRIMARY_KEY = Jdbc.class.getSimpleName();
  static final List<JdbcInterceptor> JDBC_INTERCEPTORS = new ArrayList<>(4);
  static final Map<String, DataSource> DATA_SOURCE_MAP = new ConcurrentHashMap<>(2, 1f);


  /**
   * 添加主数据源
   *
   * @param dataSource 数据源
   */
  public static void addPrimaryDataSource(DataSource dataSource) {
    addDataSource(PRIMARY_KEY, dataSource);
  }

  /**
   * 添加数据源
   *
   * @param name 数据源名称
   * @param dataSource 数据源
   */
  public static void addDataSource(String name, DataSource dataSource) {
    DataSource local = DATA_SOURCE_MAP.putIfAbsent(name, dataSource);
    if (local != null) {
      throw new IllegalArgumentException(String.format("数据源名称[%s]已存在", name));
    }
  }

  /**
   * 增加jdbc拦截
   *
   * @param interceptor 拦截
   */
  public static void addJdbcInterceptor(JdbcInterceptor interceptor) {
    JDBC_INTERCEPTORS.add(interceptor);
  }

  /**
   * 获取数据库连接
   *
   * @param dataSourceName 数据源名称
   * @return connection
   */
  public static Connection getConnection(String dataSourceName) {
    try {
      DataSource dataSource = DATA_SOURCE_MAP.get(dataSourceName);
      if (dataSource == null) {
        throw new NullPointerException();
      }
      return dataSource.getConnection();
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用primary数据源，自动关闭连接
   *
   * @param sql sql
   * @param clazz bean类型
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(String sql, Class<T> clazz, Object... params) {
    return query(PRIMARY_KEY, sql, clazz, params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用dataSourceName数据源，自动关闭连接
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param clazz bean类型
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(String dataSourceName, String sql, Class<T> clazz, Object... params) {
    return query(dataSourceName, sql, ResultSetHandler.beanHandler(clazz), params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用primary数据源，自动关闭连接
   *
   * @param sql sql
   * @param handler 结果集处理器
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(String sql, ResultSetHandler<T> handler, Object... params) {
    return query(PRIMARY_KEY, sql, handler, params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用primary数据源，自动关闭连接
   *
   * @param sql sql
   * @param handler 结果集处理器
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(String sql, ResultSetHandler<T> handler, List<Object> params) {
    return query(getConnection(PRIMARY_KEY), sql, handler, params, true);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用dataSourceName数据源，自动关闭连接
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param handler 结果集处理器
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(String dataSourceName, String sql, ResultSetHandler<T> handler,
      Object... params) {
    return query(dataSourceName, sql, handler, Arrays.asList(params));
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用dataSourceName数据源，自动关闭连接
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param handler 结果集处理器
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(String dataSourceName, String sql, ResultSetHandler<T> handler,
      List<Object> params) {
    return query(getConnection(dataSourceName), sql, handler, params, true);
  }

  /**
   * 执行 select 操作
   * <br>
   * 不关闭连接
   *
   * @param connection 数据库连接
   * @param sql sql
   * @param handler 结果集处理器
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(Connection connection, String sql, ResultSetHandler<T> handler,
      Object... params) {
    return query(connection, sql, handler, Arrays.asList(params));
  }

  /**
   * 执行 select 操作
   * <br>
   * 不关闭连接
   *
   * @param connection 数据库连接
   * @param sql sql
   * @param handler 结果集处理器
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(Connection connection, String sql, ResultSetHandler<T> handler,
      List<Object> params) {
    return query(connection, sql, handler, params, false);
  }

  /**
   * 执行 select 操作
   *
   * @param connection 数据库连接
   * @param sql sql
   * @param handler 结果集处理器
   * @param params 参数
   * @param closeCon 是否关闭连接
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(Connection connection, String sql, ResultSetHandler<T> handler,
      List<Object> params, boolean closeCon) {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    T result = null;
    try {
      before(sql, params);
      stmt = connection.prepareStatement(sql);
      fillStatement(stmt, params);
      rs = stmt.executeQuery();
      result = handler.handle(rs);
      after(sql, params, result);
    } catch (Exception e) {
      onException(sql, params, e);
      throw JdbcException.wrap(e);
    } finally {
      close(rs);
      close(stmt);
      if (closeCon) {
        close(connection);
      }
      onFinally(sql, params, result);
    }
    return result;
  }

  /**
   * 执行 select 操作 并转换为对象集合
   * <br>
   * 使用primary数据源，自动关闭连接
   *
   * @param sql sql
   * @param clazz bean类型
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> List<T> queryForList(String sql, Class<T> clazz, Object... params) {
    return queryForList(PRIMARY_KEY, sql, clazz, params);
  }

  /**
   * 执行 select 操作 并转换为对象集合
   * <br>
   * 使用dataSourceName数据源，自动关闭连接
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param clazz bean类型
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> List<T> queryForList(String dataSourceName, String sql, Class<T> clazz,
      Object... params) {
    return query(dataSourceName, sql, ResultSetHandler.beanListHandler(clazz), params);
  }

  /**
   * 执行 select 操作 并转换为对象集合
   * <br>
   * 使用primary数据源，自动关闭连接
   *
   * @param sql sql
   * @param clazz bean类型
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> List<T> queryForList(String sql, Class<T> clazz, List<Object> params) {
    return queryForList(PRIMARY_KEY, sql, clazz, params);
  }

  /**
   * 执行 select 操作 并转换为对象集合
   * <br>
   * 使用dataSourceName数据源，自动关闭连接
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param clazz bean类型
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> List<T> queryForList(String dataSourceName, String sql, Class<T> clazz,
      List<Object> params) {
    return query(dataSourceName, sql, ResultSetHandler.beanListHandler(clazz), params);
  }

  /**
   * 执行 select 操作 并转换为map
   * <br>
   * 使用primary数据源，自动关闭连接
   *
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static Map<String, Object> queryForMap(String sql, Object... params) {
    return queryForMap(PRIMARY_KEY, sql, params);
  }

  /**
   * 执行 select 操作 并转换为map
   * <br>
   * 使用dataSourceName数据源，自动关闭连接
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static Map<String, Object> queryForMap(String dataSourceName, String sql,
      Object... params) {
    return query(dataSourceName, sql, ResultSetHandler.mapHandler(), params);
  }

  /**
   * 执行 select 操作 并转换为map
   * <br>
   * 使用primary数据源，自动关闭连接
   *
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static Map<String, Object> queryForMap(String sql, List<Object> params) {
    return queryForMap(PRIMARY_KEY, sql, params);
  }

  /**
   * 执行 select 操作 并转换为map
   * <br>
   * 使用dataSourceName数据源，自动关闭连接
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static Map<String, Object> queryForMap(String dataSourceName, String sql,
      List<Object> params) {
    return query(dataSourceName, sql, ResultSetHandler.mapHandler(), params);
  }

  /**
   * 执行 select 操作 并转换为map集合
   * <br>
   * 使用primary数据源，自动关闭连接
   *
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static List<Map<String, Object>> queryForMapList(String sql, Object... params) {
    return queryForMapList(PRIMARY_KEY, sql, params);
  }

  /**
   * 执行 select 操作 并转换为map集合
   * <br>
   * 使用dataSourceName数据源，自动关闭连接
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static List<Map<String, Object>> queryForMapList(String dataSourceName, String sql,
      Object... params) {
    return query(dataSourceName, sql, ResultSetHandler.mapListHandler(), params);
  }

  /**
   * 执行 select 操作 并转换为map集合
   * <br>
   * 使用primary数据源，自动关闭连接
   *
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static List<Map<String, Object>> queryForMapList(String sql, List<Object> params) {
    return queryForMapList(PRIMARY_KEY, sql, params);
  }

  /**
   * 执行 select 操作 并转换为map集合
   * <br>
   * 使用dataSourceName数据源，自动关闭连接
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static List<Map<String, Object>> queryForMapList(String dataSourceName, String sql,
      List<Object> params) {
    return query(dataSourceName, sql, ResultSetHandler.mapListHandler(), params);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 使用primary数据源，自动关闭连接
   *
   * @param sql sql
   * @param params 参数
   * @return 受影响行数
   */
  public static int update(String sql, Object... params) {
    return update(PRIMARY_KEY, sql, params);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 使用primary数据源，自动关闭连接
   *
   * @param sql sql
   * @param params 参数
   * @return 受影响行数
   */
  public static int update(String sql, List<Object> params) {
    return update(PRIMARY_KEY, sql, params);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 使用dataSourceName数据源，自动关闭连接
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param params 参数
   * @return 受影响行数
   */
  public static int update(String dataSourceName, String sql, Object... params) {
    return update(getConnection(dataSourceName), sql, Arrays.asList(params));
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 使用dataSourceName数据源，自动关闭连接
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param params 参数
   * @return 受影响行数
   */
  public static int update(String dataSourceName, String sql, List<Object> params) {
    return update(getConnection(dataSourceName), sql, params, true);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 不关闭连接
   *
   * @param connection 数据库连接
   * @param sql sql
   * @param params 参数
   * @return 受影响行数
   */
  public static int update(Connection connection, String sql, Object... params) {
    return update(connection, sql, Arrays.asList(params), false);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 不关闭连接
   *
   * @param connection 数据库连接
   * @param sql sql
   * @param params 参数
   * @return 受影响行数
   */
  public static int update(Connection connection, String sql, List<Object> params) {
    return update(connection, sql, params, false);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   *
   * @param connection 数据库连接
   * @param sql sql
   * @param params 参数
   * @param closeCon 是否关闭连接
   * @return 受影响行数
   */
  public static int update(Connection connection, String sql, List<Object> params,
      boolean closeCon) {
    PreparedStatement stmt = null;
    int rows = 0;
    try {
      before(sql, params);
      stmt = connection.prepareStatement(sql);
      fillStatement(stmt, params);
      rows = stmt.executeUpdate();
      after(sql, params, rows);
    } catch (Exception e) {
      onException(sql, params, e);
      throw JdbcException.wrap(e);
    } finally {
      close(stmt);
      if (closeCon) {
        close(connection);
      }
      onFinally(sql, params, rows);
    }
    return rows;
  }

  private static void fillStatement(PreparedStatement stmt, List<Object> params)
      throws SQLException {
    if (params != null && !params.isEmpty()) {
      for (int i = 0, len = params.size(); i < len; i++) {
        stmt.setObject(i + 1, params.get(i));
      }
    }
  }

  /**
   * 用于关闭ResultSet PreparedStatement Connection
   *
   * @param closeable AutoCloseable
   */
  public static void close(AutoCloseable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        throw JdbcException.wrap(e);
      }
    }
  }

  private static void before(String sql, List<Object> params) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.before(sql, params);
    }
  }

  private static void after(String sql, List<Object> params, Object result) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.after(sql, params, result);
    }
  }

  private static void onException(String sql, List<Object> params, Exception e) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.onException(sql, params, e);
    }
  }

  private static void onFinally(String sql, List<Object> params, Object result) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.onFinally(sql, params, result);
    }
  }


}
