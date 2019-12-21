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
package vip.justlive.oxygen.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import vip.justlive.oxygen.jdbc.handler.ResultSetHandler;
import vip.justlive.oxygen.jdbc.interceptor.JdbcInterceptor;
import vip.justlive.oxygen.jdbc.interceptor.PageJdbcInterceptor;
import vip.justlive.oxygen.jdbc.interceptor.SqlCtx;
import vip.justlive.oxygen.jdbc.page.Page;
import vip.justlive.oxygen.jdbc.page.PageDialectHelper;

/**
 * jdbc操作类
 *
 * @author wubo
 */
public class Jdbc {

  static final String PRIMARY_KEY = Jdbc.class.getSimpleName();
  static final String TEMPLATE = "datasource.%s";
  private static final List<JdbcInterceptor> JDBC_INTERCEPTORS = new ArrayList<>(4);
  private static final Map<String, DataSource> DATA_SOURCE_MAP = new ConcurrentHashMap<>(2, 1f);
  private static final ThreadLocal<Map<String, Connection>> CONNECTION_CONTAINER = ThreadLocal
      .withInitial(ConcurrentHashMap::new);
  private static final ThreadLocal<String> CURRENT_DATASOURCE = ThreadLocal
      .withInitial(() -> PRIMARY_KEY);

  Jdbc() {
  }

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
    if (!JDBC_INTERCEPTORS.contains(PageJdbcInterceptor.PAGE_JDBC_INTERCEPTOR)) {
      JDBC_INTERCEPTORS.add(PageJdbcInterceptor.PAGE_JDBC_INTERCEPTOR);
      Collections.sort(JDBC_INTERCEPTORS);
    }
    PageDialectHelper.guess(name, dataSource);
  }

  /**
   * 增加jdbc拦截
   *
   * @param interceptor 拦截
   */
  public static void addJdbcInterceptor(JdbcInterceptor interceptor) {
    JDBC_INTERCEPTORS.add(interceptor);
    Collections.sort(JDBC_INTERCEPTORS);
  }

  /**
   * 当前线程使用默认数据源
   */
  public static void use() {
    use(PRIMARY_KEY);
  }

  /**
   * 当前线程使用指定数据源
   *
   * @param dataSourceName 数据源名称
   */
  public static void use(String dataSourceName) {
    CURRENT_DATASOURCE.set(dataSourceName);
  }

  /**
   * 当前线程使用的数据源
   *
   * @return dataSourceName
   */
  public static String currentUse() {
    return CURRENT_DATASOURCE.get();
  }

  /**
   * 还原当前线程为默认数据源
   */
  public static void clear() {
    CURRENT_DATASOURCE.remove();
  }

  /**
   * 还原当前线程数据源，并清空所有数据源连接
   */
  public static void clearAll() {
    clear();
    CONNECTION_CONTAINER.remove();
  }

  /**
   * 关闭并清除数据源
   */
  public static void shutdown() {
    clearAll();
    JDBC_INTERCEPTORS.clear();
    DATA_SOURCE_MAP.values().forEach(ds -> {
      if (ds instanceof AutoCloseable) {
        close((AutoCloseable) ds);
      }
    });
    DATA_SOURCE_MAP.clear();
    PageDialectHelper.clear();
  }

  /**
   * 获取数据库连接
   *
   * @param dataSourceName 数据源名称
   * @return connection
   */
  public static Connection getConnection(String dataSourceName) {
    Connection connection = CONNECTION_CONTAINER.get().get(dataSourceName);
    if (connection != null) {
      return connection;
    }
    try {
      DataSource dataSource = DATA_SOURCE_MAP.get(dataSourceName);
      if (dataSource == null) {
        throw new NullPointerException();
      }
      connection = dataSource.getConnection();
      CONNECTION_CONTAINER.get().putIfAbsent(dataSourceName, connection);
      return connection;
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }

  /**
   * 开启事务 默认primary数据源
   */
  public static void startTx() {
    String dataSourceName = CURRENT_DATASOURCE.get();
    startTx(dataSourceName);
  }

  /**
   * 开启指定数据源的事务
   *
   * @param dataSourceName 数据源名称
   */
  public static void startTx(String dataSourceName) {
    Connection connection = getConnection(dataSourceName);
    if (connection != null) {
      try {
        connection.setAutoCommit(false);
      } catch (SQLException e) {
        throw JdbcException.wrap(e);
      }
    }
  }

  /**
   * 关闭事务 默认primary数据源
   */
  public static void closeTx() {
    String dataSourceName = CURRENT_DATASOURCE.get();
    closeTx(dataSourceName);
  }

  /**
   * 关闭指定数据源的事务
   *
   * @param dataSourceName 数据源名称
   */
  public static void closeTx(String dataSourceName) {
    Connection connection = getConnection(dataSourceName);
    if (connection != null) {
      try {
        connection.setAutoCommit(false);
        connection.commit();
        connection.close();
      } catch (SQLException e) {
        throw JdbcException.wrap(e);
      } finally {
        removeThreadLocal(connection);
      }
    }
  }

  /**
   * 回滚事务 默认primary数据源
   */
  public static void rollbackTx() {
    String dataSourceName = CURRENT_DATASOURCE.get();
    rollbackTx(dataSourceName);
  }

  /**
   * 回滚事务
   *
   * @param dataSourceName 数据源名称
   */
  public static void rollbackTx(String dataSourceName) {
    Connection connection = getConnection(dataSourceName);
    if (connection != null) {
      try {
        connection.setAutoCommit(false);
        connection.rollback();
      } catch (SQLException e) {
        throw JdbcException.wrap(e);
      }
    }
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用primary数据源
   *
   * @param sql sql
   * @param clazz bean类型
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(String sql, Class<T> clazz, Object... params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return query(dataSourceName, sql, clazz, params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用primary数据源
   *
   * @param sql sql
   * @param clazz bean类型
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(String sql, Class<T> clazz, List<Object> params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return query(dataSourceName, sql, clazz, params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用dataSourceName数据源
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
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param clazz bean类型
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(String dataSourceName, String sql, Class<T> clazz,
      List<Object> params) {
    return query(dataSourceName, sql, ResultSetHandler.beanHandler(clazz), params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用primary数据源
   *
   * @param sql sql
   * @param handler 结果集处理器
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(String sql, ResultSetHandler<T> handler, Object... params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return query(dataSourceName, sql, handler, params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用primary数据源
   *
   * @param sql sql
   * @param handler 结果集处理器
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> T query(String sql, ResultSetHandler<T> handler, List<Object> params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return query(getConnection(dataSourceName), sql, handler, params, true);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用dataSourceName数据源
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
   * 使用dataSourceName数据源
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
    Connection connection = getConnection(dataSourceName);
    try {
      return query(connection, sql, handler, params, connection.getAutoCommit());
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
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
    SqlCtx ctx = new SqlCtx().setSql(sql).setParams(params);
    try {
      before(ctx);
      stmt = connection.prepareStatement(ctx.getSql());
      fillStatement(stmt, ctx.getParams());
      rs = stmt.executeQuery();
      result = handler.handle(rs);
      after(ctx, result);
    } catch (Exception e) {
      onException(ctx, e);
      throw JdbcException.wrap(e);
    } finally {
      close(rs);
      close(stmt);
      if (closeCon) {
        close(connection);
        removeThreadLocal(connection);
      }
      onFinally(ctx, result);
    }
    return result;
  }

  private static void removeThreadLocal(Connection connection) {
    Map<String, Connection> map = CONNECTION_CONTAINER.get();
    for (Map.Entry<String, Connection> entry : map.entrySet()) {
      if (Objects.equals(connection, entry.getValue())) {
        map.remove(entry.getKey());
        break;
      }
    }
    if (map.isEmpty()) {
      CONNECTION_CONTAINER.remove();
    }
  }


  /**
   * 执行 select 操作 并转换为对象集合
   * <br>
   * 使用primary数据源
   *
   * @param sql sql
   * @param clazz bean类型
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> List<T> queryForList(String sql, Class<T> clazz, Object... params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return queryForList(dataSourceName, sql, clazz, params);
  }

  /**
   * 执行 select 操作 并转换为对象集合
   * <br>
   * 使用dataSourceName数据源
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
   * 使用primary数据源
   *
   * @param sql sql
   * @param clazz bean类型
   * @param params 参数
   * @param <T> 泛型
   * @return result
   */
  public static <T> List<T> queryForList(String sql, Class<T> clazz, List<Object> params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return queryForList(dataSourceName, sql, clazz, params);
  }

  /**
   * 执行 select 操作 并转换为对象集合
   * <br>
   * 使用dataSourceName数据源
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
   * 使用primary数据源
   *
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static Map<String, Object> queryForMap(String sql, Object... params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return queryForMap(dataSourceName, sql, params);
  }

  /**
   * 执行 select 操作 并转换为map
   * <br>
   * 使用dataSourceName数据源
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
   * 使用primary数据源
   *
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static Map<String, Object> queryForMap(String sql, List<Object> params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return queryForMap(dataSourceName, sql, params);
  }

  /**
   * 执行 select 操作 并转换为map
   * <br>
   * 使用dataSourceName数据源
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
   * 使用primary数据源
   *
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static List<Map<String, Object>> queryForMapList(String sql, Object... params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return queryForMapList(dataSourceName, sql, params);
  }

  /**
   * 执行 select 操作 并转换为map集合
   * <br>
   * 使用dataSourceName数据源
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
   * 使用primary数据源
   *
   * @param sql sql
   * @param params 参数
   * @return result
   */
  public static List<Map<String, Object>> queryForMapList(String sql, List<Object> params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return queryForMapList(dataSourceName, sql, params);
  }

  /**
   * 执行 select 操作 并转换为map集合
   * <br>
   * 使用dataSourceName数据源
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
   * 使用primary数据源
   *
   * @param sql sql
   * @param params 参数
   * @return 受影响行数
   */
  public static int update(String sql, Object... params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return update(dataSourceName, sql, params);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 使用primary数据源
   *
   * @param sql sql
   * @param params 参数
   * @return 受影响行数
   */
  public static int update(String sql, List<Object> params) {
    String dataSourceName = CURRENT_DATASOURCE.get();
    return update(dataSourceName, sql, params);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param params 参数
   * @return 受影响行数
   */
  public static int update(String dataSourceName, String sql, Object... params) {
    Connection connection = getConnection(dataSourceName);
    try {
      return update(connection, sql, Arrays.asList(params), connection.getAutoCommit());
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql sql
   * @param params 参数
   * @return 受影响行数
   */
  public static int update(String dataSourceName, String sql, List<Object> params) {
    Connection connection = getConnection(dataSourceName);
    try {
      return update(connection, sql, params, connection.getAutoCommit());
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
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
    SqlCtx ctx = new SqlCtx().setSql(sql).setParams(params);
    try {
      before(ctx);
      stmt = connection.prepareStatement(ctx.getSql());
      fillStatement(stmt, ctx.getParams());
      rows = stmt.executeUpdate();
      after(ctx, rows);
    } catch (Exception e) {
      onException(ctx, e);
      throw JdbcException.wrap(e);
    } finally {
      close(stmt);
      if (closeCon) {
        close(connection);
        removeThreadLocal(connection);
      }
      onFinally(ctx, rows);
    }
    return rows;
  }

  static void fillStatement(PreparedStatement stmt, List<Object> params) throws SQLException {
    if (params != null && !params.isEmpty()) {
      for (int i = 0, len = params.size(); i < len; i++) {
        if (params.get(i) instanceof Page) {
          continue;
        }
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

  private static void before(SqlCtx ctx) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.before(ctx);
    }
  }

  private static void after(SqlCtx ctx, Object result) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.after(ctx, result);
    }
  }

  private static void onException(SqlCtx ctx, Exception e) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.onException(ctx, e);
    }
  }

  private static void onFinally(SqlCtx ctx, Object result) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.onFinally(ctx, result);
    }
  }
}
