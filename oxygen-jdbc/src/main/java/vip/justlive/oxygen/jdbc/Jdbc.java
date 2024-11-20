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
package vip.justlive.oxygen.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.jdbc.handler.ResultSetHandler;
import vip.justlive.oxygen.jdbc.interceptor.JdbcInterceptor;
import vip.justlive.oxygen.jdbc.interceptor.OrderJdbcInterceptor;
import vip.justlive.oxygen.jdbc.interceptor.PageJdbcInterceptor;
import vip.justlive.oxygen.jdbc.interceptor.SqlCtx;
import vip.justlive.oxygen.jdbc.page.Page;
import vip.justlive.oxygen.jdbc.page.PageDialectHelper;

/**
 * jdbc操作类
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class Jdbc {

  public final String PRIMARY_KEY = Jdbc.class.getSimpleName();

  final String TEMPLATE = "oxygen.datasource.%s";
  private final List<JdbcInterceptor> JDBC_INTERCEPTORS = new ArrayList<>(4);
  private final Map<String, DataSource> DATA_SOURCE_MAP = new ConcurrentHashMap<>(2, 1f);
  private final ThreadLocal<Map<String, Connection>> CONNECTION_CONTAINER = ThreadLocal.withInitial(
      ConcurrentHashMap::new);
  private final ThreadLocal<String> CURRENT_DATASOURCE = ThreadLocal.withInitial(() -> PRIMARY_KEY);


  /**
   * 添加主数据源
   *
   * @param dataSource 数据源
   */
  public void addPrimaryDataSource(DataSource dataSource) {
    addDataSource(PRIMARY_KEY, dataSource);
  }

  /**
   * 添加数据源
   *
   * @param name       数据源名称
   * @param dataSource 数据源
   */
  public void addDataSource(String name, DataSource dataSource) {
    DataSource local = DATA_SOURCE_MAP.putIfAbsent(name, dataSource);
    if (local != null) {
      throw new IllegalArgumentException(String.format("数据源名称[%s]已存在", name));
    }
    log.info("add datasource [{}]", name);
    if (!JDBC_INTERCEPTORS.contains(PageJdbcInterceptor.PAGE_JDBC_INTERCEPTOR)) {
      JDBC_INTERCEPTORS.add(PageJdbcInterceptor.PAGE_JDBC_INTERCEPTOR);
      Collections.sort(JDBC_INTERCEPTORS);
    }
    if (!JDBC_INTERCEPTORS.contains(OrderJdbcInterceptor.ORDER_JDBC_INTERCEPTOR)) {
      JDBC_INTERCEPTORS.add(OrderJdbcInterceptor.ORDER_JDBC_INTERCEPTOR);
      Collections.sort(JDBC_INTERCEPTORS);
    }
    PageDialectHelper.guess(name, dataSource);
  }

  /**
   * 增加jdbc拦截
   *
   * @param interceptor 拦截
   */
  public void addJdbcInterceptor(JdbcInterceptor interceptor) {
    JDBC_INTERCEPTORS.add(interceptor);
    Collections.sort(JDBC_INTERCEPTORS);
  }

  /**
   * 当前线程使用默认数据源
   */
  public void use() {
    use(PRIMARY_KEY);
  }

  /**
   * 当前线程使用指定数据源
   *
   * @param dataSourceName 数据源名称
   */
  public void use(String dataSourceName) {
    CURRENT_DATASOURCE.set(dataSourceName);
  }

  /**
   * 当前线程使用的数据源
   *
   * @return dataSourceName
   */
  public String currentUse() {
    return CURRENT_DATASOURCE.get();
  }

  /**
   * 还原当前线程为默认数据源
   */
  public void clear() {
    CURRENT_DATASOURCE.remove();
  }

  /**
   * 还原当前线程数据源，并清空所有数据源连接
   */
  public void clearAll() {
    clear();
    CONNECTION_CONTAINER.remove();
  }

  /**
   * 关闭并清除数据源
   */
  public void shutdown() {
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
  public Connection getConnection(String dataSourceName) {
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
  public void startTx() {
    startTx(currentUse());
  }

  /**
   * 开启指定数据源的事务
   *
   * @param dataSourceName 数据源名称
   */
  public void startTx(String dataSourceName) {
    startTx(getConnection(dataSourceName));
  }

  /**
   * 开启指定数据源的事务
   *
   * @param connection 数据库连接
   */
  public void startTx(Connection connection) {
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
  public void closeTx() {
    closeTx(currentUse());
  }

  /**
   * 关闭指定数据源的事务
   *
   * @param dataSourceName 数据源名称
   */
  public void closeTx(String dataSourceName) {
    closeTx(getConnection(dataSourceName));
  }

  /**
   * 关闭指定数据源的事务
   *
   * @param connection 数据源连接
   */
  public void closeTx(Connection connection) {
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
  public void rollbackTx() {
    rollbackTx(currentUse());
  }

  /**
   * 回滚事务
   *
   * @param dataSourceName 数据源名称
   */
  public void rollbackTx(String dataSourceName) {
    rollbackTx(getConnection(dataSourceName));
  }

  /**
   * 回滚指定数据源的事务
   *
   * @param connection 数据源连接
   */
  public void rollbackTx(Connection connection) {
    if (connection != null) {
      try {
        connection.setAutoCommit(false);
        connection.rollback();
      } catch (SQLException e) {
        throw JdbcException.wrap(e);
      } finally {
        close(connection);
      }
    }
  }

  /**
   * 在事务中执行
   *
   * @param runnable 执行逻辑
   */
  public void invokeWithinTx(Runnable runnable) {
    startTx();
    boolean hasError = false;
    try {
      runnable.run();
    } catch (Exception e) {
      hasError = true;
      throw e;
    } finally {
      if (hasError) {
        rollbackTx();
      } else {
        closeTx();
      }
    }
  }

  /**
   * 在事务中执行
   *
   * @param runnable   执行逻辑
   * @param datasource 数据源
   */
  public void invokeWithinTx(Runnable runnable, String datasource) {
    startTx(datasource);
    boolean hasError = false;
    try {
      runnable.run();
    } catch (Exception e) {
      hasError = true;
      throw e;
    } finally {
      if (hasError) {
        rollbackTx(datasource);
      } else {
        closeTx(datasource);
      }
    }
  }

  /**
   * 在事务中执行
   *
   * @param runnable   执行逻辑
   * @param connection 数据库连接
   */
  public void invokeWithinTx(Runnable runnable, Connection connection) {
    startTx(connection);
    boolean hasError = false;
    try {
      runnable.run();
    } catch (Exception e) {
      hasError = true;
      throw e;
    } finally {
      if (hasError) {
        rollbackTx(connection);
      } else {
        closeTx(connection);
      }
    }
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用primary数据源
   *
   * @param sql    sql
   * @param clazz  bean类型
   * @param params 参数
   * @param <T>    泛型
   * @return result
   */
  public <T> T query(String sql, Class<T> clazz, Object... params) {
    return query(currentUse(), sql, clazz, params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用primary数据源
   *
   * @param sql    sql
   * @param clazz  bean类型
   * @param params 参数
   * @param <T>    泛型
   * @return result
   */
  public <T> T query(String sql, Class<T> clazz, List<Object> params) {
    return query(currentUse(), sql, clazz, params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql            sql
   * @param clazz          bean类型
   * @param params         参数
   * @param <T>            泛型
   * @return result
   */
  public <T> T query(String dataSourceName, String sql, Class<T> clazz, Object... params) {
    return query(dataSourceName, sql, ResultSetHandler.beanHandler(clazz), params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql            sql
   * @param clazz          bean类型
   * @param params         参数
   * @param <T>            泛型
   * @return result
   */
  public <T> T query(String dataSourceName, String sql, Class<T> clazz, List<Object> params) {
    return query(dataSourceName, sql, ResultSetHandler.beanHandler(clazz), params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用primary数据源
   *
   * @param sql     sql
   * @param handler 结果集处理器
   * @param params  参数
   * @param <T>     泛型
   * @return result
   */
  public <T> T query(String sql, ResultSetHandler<T> handler, Object... params) {
    return query(currentUse(), sql, handler, params);
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用primary数据源
   *
   * @param sql     sql
   * @param handler 结果集处理器
   * @param params  参数
   * @param <T>     泛型
   * @return result
   */
  public <T> T query(String sql, ResultSetHandler<T> handler, List<Object> params) {
    Connection conn = getConnection(currentUse());
    return query(conn, sql, handler, params, isAutoCommit(conn));
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql            sql
   * @param handler        结果集处理器
   * @param params         参数
   * @param <T>            泛型
   * @return result
   */
  public <T> T query(String dataSourceName, String sql, ResultSetHandler<T> handler,
      Object... params) {
    return query(dataSourceName, sql, handler, Arrays.asList(params));
  }

  /**
   * 执行 select 操作
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql            sql
   * @param handler        结果集处理器
   * @param params         参数
   * @param <T>            泛型
   * @return result
   */
  public <T> T query(String dataSourceName, String sql, ResultSetHandler<T> handler,
      List<Object> params) {
    Connection connection = getConnection(dataSourceName);
    return query(connection, sql, handler, params, isAutoCommit(connection));
  }

  /**
   * 执行 select 操作
   *
   * @param connection 数据库连接
   * @param sql        sql
   * @param handler    结果集处理器
   * @param params     参数
   * @param <T>        泛型
   * @return result
   */
  public <T> T query(Connection connection, String sql, ResultSetHandler<T> handler,
      Object... params) {
    return query(connection, sql, handler, Arrays.asList(params));
  }

  /**
   * 执行 select 操作
   *
   * @param connection 数据库连接
   * @param sql        sql
   * @param handler    结果集处理器
   * @param params     参数
   * @param <T>        泛型
   * @return result
   */
  public <T> T query(Connection connection, String sql, ResultSetHandler<T> handler,
      List<Object> params) {
    return query(connection, sql, handler, params, isAutoCommit(connection));
  }

  /**
   * 执行 select 操作
   *
   * @param connection 数据库连接
   * @param sql        sql
   * @param handler    结果集处理器
   * @param params     参数
   * @param closeCon   是否关闭连接
   * @param <T>        泛型
   * @return result
   */
  public <T> T query(Connection connection, String sql, ResultSetHandler<T> handler,
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
      }
      onFinally(ctx, result);
    }
    return result;
  }

  private void removeThreadLocal(Connection connection) {
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
   * @param sql    sql
   * @param clazz  bean类型
   * @param params 参数
   * @param <T>    泛型
   * @return result
   */
  public <T> List<T> queryForList(String sql, Class<T> clazz, Object... params) {
    return queryForList(currentUse(), sql, clazz, params);
  }

  /**
   * 执行 select 操作 并转换为对象集合
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql            sql
   * @param clazz          bean类型
   * @param params         参数
   * @param <T>            泛型
   * @return result
   */
  public <T> List<T> queryForList(String dataSourceName, String sql, Class<T> clazz,
      Object... params) {
    return query(dataSourceName, sql, ResultSetHandler.beanListHandler(clazz), params);
  }

  /**
   * 执行 select 操作 并转换为对象集合
   * <br>
   * 使用primary数据源
   *
   * @param sql    sql
   * @param clazz  bean类型
   * @param params 参数
   * @param <T>    泛型
   * @return result
   */
  public <T> List<T> queryForList(String sql, Class<T> clazz, List<Object> params) {
    return queryForList(currentUse(), sql, clazz, params);
  }

  /**
   * 执行 select 操作 并转换为对象集合
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql            sql
   * @param clazz          bean类型
   * @param params         参数
   * @param <T>            泛型
   * @return result
   */
  public <T> List<T> queryForList(String dataSourceName, String sql, Class<T> clazz,
      List<Object> params) {
    return query(dataSourceName, sql, ResultSetHandler.beanListHandler(clazz), params);
  }

  /**
   * 执行 select 操作 并转换为map
   * <br>
   * 使用primary数据源
   *
   * @param sql    sql
   * @param params 参数
   * @return result
   */
  public Map<String, Object> queryForMap(String sql, Object... params) {
    return queryForMap(currentUse(), sql, params);
  }

  /**
   * 执行 select 操作 并转换为map
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql            sql
   * @param params         参数
   * @return result
   */
  public Map<String, Object> queryForMap(String dataSourceName, String sql, Object... params) {
    return query(dataSourceName, sql, ResultSetHandler.mapHandler(), params);
  }

  /**
   * 执行 select 操作 并转换为map
   * <br>
   * 使用primary数据源
   *
   * @param sql    sql
   * @param params 参数
   * @return result
   */
  public Map<String, Object> queryForMap(String sql, List<Object> params) {
    return queryForMap(currentUse(), sql, params);
  }

  /**
   * 执行 select 操作 并转换为map
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql            sql
   * @param params         参数
   * @return result
   */
  public Map<String, Object> queryForMap(String dataSourceName, String sql, List<Object> params) {
    return query(dataSourceName, sql, ResultSetHandler.mapHandler(), params);
  }

  /**
   * 执行 select 操作 并转换为map集合
   * <br>
   * 使用primary数据源
   *
   * @param sql    sql
   * @param params 参数
   * @return result
   */
  public List<Map<String, Object>> queryForMapList(String sql, Object... params) {
    return queryForMapList(currentUse(), sql, params);
  }

  /**
   * 执行 select 操作 并转换为map集合
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql            sql
   * @param params         参数
   * @return result
   */
  public List<Map<String, Object>> queryForMapList(String dataSourceName, String sql,
      Object... params) {
    return query(dataSourceName, sql, ResultSetHandler.mapListHandler(), params);
  }

  /**
   * 执行 select 操作 并转换为map集合
   * <br>
   * 使用primary数据源
   *
   * @param sql    sql
   * @param params 参数
   * @return result
   */
  public List<Map<String, Object>> queryForMapList(String sql, List<Object> params) {
    return queryForMapList(currentUse(), sql, params);
  }

  /**
   * 执行 select 操作 并转换为map集合
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql            sql
   * @param params         参数
   * @return result
   */
  public List<Map<String, Object>> queryForMapList(String dataSourceName, String sql,
      List<Object> params) {
    return query(dataSourceName, sql, ResultSetHandler.mapListHandler(), params);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 使用primary数据源
   *
   * @param sql    sql
   * @param params 参数
   * @return 受影响行数
   */
  public int update(String sql, Object... params) {
    return update(currentUse(), sql, params);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 使用primary数据源
   *
   * @param sql    sql
   * @param params 参数
   * @return 受影响行数
   */
  public int update(String sql, List<Object> params) {
    return update(currentUse(), sql, params);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   * <br>
   * 使用dataSourceName数据源
   *
   * @param dataSourceName 数据源名称
   * @param sql            sql
   * @param params         参数
   * @return 受影响行数
   */
  public int update(String dataSourceName, String sql, Object... params) {
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
   * @param sql            sql
   * @param params         参数
   * @return 受影响行数
   */
  public int update(String dataSourceName, String sql, List<Object> params) {
    Connection connection = getConnection(dataSourceName);
    try {
      return update(connection, sql, params, connection.getAutoCommit());
    } catch (SQLException e) {
      throw JdbcException.wrap(e);
    }
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   *
   * @param connection 数据库连接
   * @param sql        sql
   * @param params     参数
   * @return 受影响行数
   */
  public int update(Connection connection, String sql, Object... params) {
    return update(connection, sql, Arrays.asList(params), isAutoCommit(connection));
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   *
   * @param connection 数据库连接
   * @param sql        sql
   * @param params     参数
   * @return 受影响行数
   */
  public int update(Connection connection, String sql, List<Object> params) {
    return update(connection, sql, params, isAutoCommit(connection));
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   *
   * @param connection 数据库连接
   * @param sql        sql
   * @param params     参数
   * @param closeCon   是否关闭连接
   * @return 受影响行数
   */
  public int update(Connection connection, String sql, List<Object> params, boolean closeCon) {
    return update(connection, sql, params, closeCon, false);
  }

  /**
   * 执行 INSERT、 UPDATE、 DELETE 操作
   *
   * @param connection             数据库连接
   * @param sql                    sql
   * @param params                 参数
   * @param closeCon               是否关闭连接
   * @param returnAutoGeneratedKey 是否返回主键
   * @return 受影响行数或主键
   */
  public int update(Connection connection, String sql, List<Object> params, boolean closeCon,
      boolean returnAutoGeneratedKey) {
    PreparedStatement stmt = null;
    int rows = 0;
    SqlCtx ctx = new SqlCtx().setSql(sql).setParams(params);
    try {
      before(ctx);
      stmt = connection.prepareStatement(ctx.getSql(),
          returnAutoGeneratedKey ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
      fillStatement(stmt, ctx.getParams());
      rows = stmt.executeUpdate();
      after(ctx, rows);
      if (returnAutoGeneratedKey) {
        rows = getAutoGeneratedKey(stmt);
      }
    } catch (Exception e) {
      onException(ctx, e);
      throw JdbcException.wrap(e);
    } finally {
      close(stmt);
      if (closeCon) {
        close(connection);
      }
      onFinally(ctx, rows);
    }
    return rows;
  }

  /**
   * 是否自动提交事物
   *
   * @param conn 连接
   * @return auto commit
   */
  public boolean isAutoCommit(Connection conn) {
    try {
      return conn.getAutoCommit();
    } catch (SQLException e) {
      log.error("conn error", e);
      return true;
    }
  }

  int getAutoGeneratedKey(PreparedStatement stmt) throws SQLException {
    try (ResultSet rs = stmt.getGeneratedKeys()) {
      int autoIncrease = -1;
      if (rs.next()) {
        autoIncrease = rs.getInt(1);
      }
      return autoIncrease;
    }
  }

  void fillStatement(PreparedStatement stmt, List<Object> params) throws SQLException {
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
  public void close(AutoCloseable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        throw JdbcException.wrap(e);
      } finally {
        if (closeable instanceof Connection) {
          removeThreadLocal((Connection) closeable);
        }
      }
    }

  }

  private void before(SqlCtx ctx) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.before(ctx);
    }
  }

  private void after(SqlCtx ctx, Object result) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.after(ctx, result);
    }
  }

  private void onException(SqlCtx ctx, Exception e) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.onException(ctx, e);
    }
  }

  private void onFinally(SqlCtx ctx, Object result) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.onFinally(ctx, result);
    }
  }
}
