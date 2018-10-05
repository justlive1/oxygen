# oxygen-jdbc

轻量级jdbc实现


## 介绍

一个轻量级jdbc实现

oxygen-jdbc jdbc实现
- 小巧简单的jdbc实现，纯jdk实现，无第三方jar
- 支持多数据源
- 基于sql进行crud

```
├─ src/main
  │─ java/.../jdbc  //oxygen-jdbc代码目录
  │  │- config  //配置数据源目录
  │  │- handler  //处理器目录，包括结果集处理 行处理 列处理
  │  │- interceptor  //拦截器目录，拦截sql执行前后及异常
  │  │- Jdbc.java  //Jdbc核心操作类，提供crud操作
  │  │- JdbcException.java  //jdbc异常封装
  │  └─ JdbcPlugin.java   //jdbc插件，与oxygen-core配套使用
  └─ resources/META-INF/services
     │- ...handler.ColumnHandler //列处理服务配置文件
     └─ ...core.Plugin  //增加jdbcPlugin服务实现，与oxygen-core配套使用
  
```

## 特性

* 轻量级，注释完善，使用简单
* 使用ServiceLoader加载插件，易于扩展


## 安装

添加依赖到你的 pom.xml:
```
<!-- jdbc实现 可单独使用 -->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-jdbc</artifactId>
    <version>${oxygen.version}</version>
</dependency>

```

## 快速开始

### 基础使用

- 可配置多数据源
- 使用Jdbc进行crud
- 使用ResultSetHandler<T> 进行自定义类型转换处理
- 开启关闭事务

```
// 单独使用 需要自己创建并添加数据源
...
// 添加主数据源
Jdbc.addPrimaryDataSource(DataSource dataSource)
// 添加多数据源
Jdbc.addDataSource(String name, DataSource dataSource)

// crud
T Jdbc.query(String sql, Class<T> clazz, Object... params)
List<T> Jdbc.queryForList(String sql, Class<T> clazz, Object... params)
Map<String, Object> Jdbc.queryForMap(String sql, Object... params)
List<Map<String, Object>> Jdbc.queryForMapList(String sql, Object... params)
// 可自定义返回处理
T Jdbc.query(String sql, ResultSetHandler<T> handler, Object... params)

int Jdbc.update(String sql, Object... params)

// 开启主数据源的事务
Jdbc.startTx()
// 开启指定数据源的事务
Jdbc.startTx(String dataSourceName)

// 关闭主数据源的事务
Jdbc.closeTx()
// 关闭指定数据源的事务
Jdbc.closeTx(String dataSourceName)


// 配合oxygen-core使用, 只需在配置文件中配置数据源即可自动装载
// 多数据源名称
datasource.multi=a
// 主数据源
datasource.logSql=true
datasource.driverClassName=org.h2.Driver
datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1
datasource.username=sa
datasource.password=sa

// 数据源a
datasource.a.driverClassName=org.h2.Driver
datasource.a.url=jdbc:h2:mem:a;DB_CLOSE_DELAY=-1
datasource.a.username=sa
datasource.a.password=sa

```

### 自定义列类型转换

- 实现 `ColumnHandler` 接口进行自定义处理
- 增加 META-INF/services/vip.justlive.oxygen.jdbc.handler.ColumnHandler文件，添加实现类的类名

```
public class MyColumnHandler implements ColumnHandler {

  @Override
  public boolean supported(Class<?> type) {
    ...
  }

  @Override
  public Object fetch(ResultSet rs, int index) throws SQLException {
    ...
  }
}

// 新增或修改 META-INF/services/vip.justlive.oxygen.jdbc.handler.ColumnHandler 添加自定义类名
xxx.xxx.MyColumnHandler

```

### 增加jdbc拦截

- 实现 `JdbcInterceptor` 接口
- 调用 `Jdbc.addJdbcInterceptor` 添加拦截

```
// 内置的sql打印拦截
@Slf4j
public class LogSqlJdbcInterceptor implements JdbcInterceptor {

  @Override
  public void before(String sql, List<Object> params) {
    if (log.isDebugEnabled()) {
      log.debug("execute sql: {} -> params: {}", sql, params);
    }
  }
}

// 添加拦截器
Jdbc.addJdbcInterceptor(JdbcInterceptor interceptor)

```
