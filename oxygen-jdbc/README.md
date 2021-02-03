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
  │  │- page  //分页方言
  │  │- record  //基础crud
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
### 配置数据源
#### 单独使用jdbc不依赖oxygen-core
```
// 添加主数据源
Jdbc.addPrimaryDataSource(DataSource dataSource)
// 添加多数据源
Jdbc.addDataSource(String name, DataSource dataSource)
```

#### 配合oxygen-core自动装载
```
// 配置文件中增加如下信息
// 主数据源
oxygen.datasource.logSql=true
oxygen.datasource.driverClassName=org.h2.Driver
oxygen.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1
oxygen.datasource.username=sa
oxygen.datasource.password=sa

// 多数据源名称
oxygen.datasource.multi=a
// 数据源a
oxygen.datasource.a.driverClassName=org.h2.Driver
oxygen.datasource.a.url=jdbc:h2:mem:a;DB_CLOSE_DELAY=-1
oxygen.datasource.a.username=sa
oxygen.datasource.a.password=sa
```

#### 切换数据源
```
// 切换当前线程的数据源
Jdbc.use(dataSourceName)
// 还原当前线程的数据源
Jdbc.clear()
// 获取当前线程使用的数据源
Jdbc.currentUse()
```

### CRUD
```
// 执行select操作
// 查询单条记录
Map<String, Object> Jdbc.queryForMap(String sql, Object... params)
// 查询多条记录
List<Map<String, Object>> Jdbc.queryForMapList(String sql, Object... params)
// 查询单条记录并绑定到对象
T Jdbc.query(String sql, Class<T> clazz, Object... params)
// 查询多条记录并绑定对象
List<T> Jdbc.queryForList(String sql, Class<T> clazz, Object... params)
// 可自定义返回处理
T Jdbc.query(String sql, ResultSetHandler<T> handler, Object... params)

// 执行insert、update、delete和ddl操作
// 主库操作
Jdbc.update(String sql, Object... params)
Jdbc.update(String sql, List<Object> params)
// 指定数据源操作
Jdbc.update(String dataSourceName, String sql, Object... params)
Jdbc.update(String dataSourceName, String sql, List<Object> params)

//Example
// 删除表
Jdbc.update("drop table if exists option");
// 建表
Jdbc.update("create table option (id int primary key, st varchar, dt timestamp)");
// 插入记录
Jdbc.update("insert into option values (1, 'st', CURRENT_TIME)");
// 查询记录
Map<String, Object> record = Jdbc.queryForMap("select * from option where id = ?", 1);
List<Map<String, Object>> list = Jdbc.queryForMapList("select * from option where id = ?", 1);
Object[] record = Jdbc.query("select * from option where id = ?", ResultSetHandler.arrayHandler(), 1);
List<Object[]> list = Jdbc.query("select * from option where id = ?", ResultSetHandler.arrayListHandler(), 1);
Option option = Jdbc.query("select * from option where id = ?", Option.class, 1);
List<Option> list = Jdbc.queryForList("select * from option", Option.class);
// 分页查询，（约定参数列表第一个为分页参数）
Page<Option> page = new Page<>(1, 10);
List<Option> list = Jdbc.queryForList("select * from option", Option.class, page);
List<Map<String, Object>> list = Jdbc.queryForMapList("select * from option where st = ?", page, 'xxx');

```
### ORM
- 查询可直接使用 `T Jdbc.query(String sql, Class<T> clazz, Object... params)`等进行对象绑定
- 可通过`Record`辅助类进行对象操作
  - `@Table` 表名注解
  - `@Column` 列名注解

```
// entity
@Data
@Accessors(chain = true)
@Table
public class Option {

  @Column(pk = true)
  private Long id;

  @Column
  private String st;
}


Option option = new Option().setSt("ss");

// 插入
Record.insert(option);

// 查询
Option result = Record.findById(Option.class, 1);
List<Option> list = Record.findByIds(Option.class, Arrays.asList(1, 2));
int count = Record.count(option);
List<Option> list = Record.find(option);
List<Option> list = Record.findAll(Option.class);
Option result =  Record.findOne(option);

// 修改, 将实体中非空字段作为set字段，需要设置主键
Record.updateById(new Option().setId(1).setSt("xxx"));
// 删除
Record.deleteById(Option.class, 1);
Record.delete(option);
```

### 批量操作

```
// 开启批处理
Batch.use()
// 添加预处理sql
.addBatch("insert into system (key, value) values (?,?)", 1, 3)
.addBatch("insert into system (key, value) values (?,?)", Arrays.asList(4, 4))
.addBatch("insert into system (key,value) values(1,2)")
// 提交批处理
.commit();

// 使用record批处理
List<System> list = new ArrayList<>();
 // add ...
Record.insertBatch(list);
```

### 开启关闭事务
```

// 开启主数据源的事务
Jdbc.startTx()
// 开启指定数据源的事务
Jdbc.startTx(String dataSourceName)

// 关闭主数据源的事务
Jdbc.closeTx()
// 关闭指定数据源的事务
Jdbc.closeTx(String dataSourceName)

// 回滚事务
Jdbc.rollbackTx()
// 回滚指定数据源的事务
Jdbc.rollbackTx(String dataSourceName)

```

### 自定义类型转换
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
```
### 自定义属性转换
- 实现 `PropertyHandler` 接口进行自定义处理
- 增加 META-INF/services/vip.justlive.oxygen.jdbc.handler.PropertyHandler文件，添加实现类的类名

```
public class MyPropertyHandler implements PropertyHandler {

  @Override
  public boolean supported(Class<?> type, Object value) {
    ...
  }

  @Override
  public Object cast(Class<?> type, Object value) {
    ...
  }
}
```
### 自定义拦截器
- 实现 `JdbcInterceptor` 接口
- 调用 `Jdbc.addJdbcInterceptor` 添加拦截
- 配合oxygen的ioc可以直接增加注解`@Bean`

```
// 内置的sql打印拦截
@Slf4j
public class LogSqlJdbcInterceptor implements JdbcInterceptor {

  @Override
  public void before(SqlCtx ctx) {
    if (log.isDebugEnabled()) {
      log.debug("execute sql: {} -> params: {}", ctx.getSql(), ctx.getParams());
    }
  }
}

// 添加拦截器
Jdbc.addJdbcInterceptor(JdbcInterceptor interceptor)
```