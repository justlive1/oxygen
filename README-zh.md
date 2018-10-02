# oxygen

轻量级Java框架


## 介绍

一个轻量级Java框架

oxygen-core 核心部分
- 基于cglib的aop实现
- 提供缓存管理和基于注解的缓存，内置LocalCache和Ehcache实现，可扩展
- 配置管理，支持${attrs.key:defaultValue}表达式获取配置
- 加解密管理，提供加解密服务内置基础加密实现，例如SHA-1、SHA-256、MD5
- 异常管理，提供异常包装，统一异常编码，便于国际化
- 提供基于构造器注入的ioc(原因：依赖链清晰，并可任意切换ioc实现)
- 定时任务服务，内置提供了基于注解的定时任务服务

```
├─ src/main
  │─ java/.../core  //oxygen-core代码目录
  │  │- aop //aop实现目录
  │  │- cache //缓存实现目录
  │  │- config  //配置实现目录
  │  │- constant  //常量目录
  │  │- convert  //类型转换实现目录
  │  │- crypto  //密码加密目录
  │  │- domain  //基础实体目录
  │  │- exception  //异常管理目录
  │  │- io  //io读写目录
  │  │- ioc  //ioc实现目录
  │  │- job  //定时任务实现目录
  │  │- scan  //类扫描实现目录
  │  │- util  //工具类目录
  │  │- Bootstrap.java  //框架启动引导类
  │  └─ Plugin.java   //插件接口
  └─ resources/META-INF/services
     └─ ...core.Plugin  //Plugin服务实现配置文件
  
```

oxygen-jdbc jdbc实现
- 小巧简单的jdbc实现，纯jdk实现，无第三方jar
- 支持多数据源
- 基于sql进行crud，不提供类似Hibernate的链式方法（原因：sql作为数据库领域的DSL，已经很自然优雅，Less is more）

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
<!-- 核心包 包含aop ioc 异常处理 缓存 定时任务等 -->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-core</artifactId>
    <version>${oxygen.version}</version>
</dependency>

<!-- jdbc实现 可单独使用 -->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-jdbc</artifactId>
    <version>${oxygen.version}</version>
</dependency>

```

## 快速开始

### 基础返回

使用 `Resp` 作为返回

```
// 成功返回 code 00000
Resp.success(Object obj);

// 错误返回 默认code 99999
Resp.error(String msg);

// 错误返回 自定义code
Resp.error(String code, String msg);
```

### 异常处理

使用 `Exceptions` 抛出异常

```
// 创建 ErrorCode
ErrorCode err = Exceptions.errorCode(String module, String code);
ErrorCode err = Exceptions.errorMessage(String module, String code, String message);

// 抛出unchecked异常
throw Exceptions.wrap(Throwable e);
throw Exceptions.wrap(Throwable e, String code, String message);
throw Exceptions.wrap(Throwable e, ErrorCode errorCode, Object... arguments);

// 抛出业务异常 不含堆栈信息
throw Exceptions.fail(ErrorCode errCode, Object... params);
throw Exceptions.fail(String code, String message, Object... params);

// 抛出故障异常 包含堆栈信息
throw Exceptions.fault(ErrorCode errCode, Object... params);
throw Exceptions.fault(String code, String message, Object... params);
throw Exceptions.fault(Throwable e, ErrorCode errCode, Object... params);
throw Exceptions.fault(Throwable e, String code, String message, Object... params)

```

### IOC 

通过注解使用IOC容器

```
// 在配置文件中添加扫包路径
main.class.scan=com.xxx.xxx,com.aaa.bbb

// 使用 @Configuration 和 @Bean
@Configuration
public class Conf {
 
  @Bean
  Inter noDepBean() {
    return new NoDepBean();
  }
}

// 使用 @Bean 和 @Inject
@Bean("depBean")
public class DepBean implements Inter {

  private final NoDepBean noDepBean;

  @Inject
  public DepBean(NoDepBean noDepBean) {
    this.noDepBean = noDepBean;
  }
  
  ...
}

// 运行时获取bean
Inter inter = BeanStore.getBean("depBean", Inter.class);

```

### AOP

通过注解使用AOP

```
// 定义使用了Log注解的方法aop处理
@Before(annotation = Log.class)
public void log(Invocation invocation) {
  ...
}

// 目标方法添加注解
@Log
public void print() {
  ...
}  
```

### 定时任务

使用注解 `@Scheduled` 标记一个方法需要作为定时任务

onApplicationStart(), cron(), fixedDelay(), or fixedRate() 必须配置其中一个

```
// 固定延迟任务 任务结束时间-下一个开始时间间隔固定
@Scheduled(fixedDelay = "500")
public void run1() {
  ...
}

// 固定周期任务 任务开始时间-下一个开始时间固定
@Scheduled(fixedRate = "600")
public void run2() {
  ...
}

// cron任务，并且程序启动后异步执行一次
@Scheduled(cron = "0/5 * * * * ?", onApplicationStart = true, async = true)
public void run3() {
  ...
}
```

### 缓存

使用缓存有两种方式：
- `JCache.cache()` 获取缓存然后调用api
- 使用 `@Cacheable` 注解给方法添加缓存

```
// 使用缓存api 
Cache cache = JCache.cache(cacheName);
T value = cache.get(key, clazz);
cache.set(key, value, duration, timeUnit);
...

// 使用注解
@Cacheable
public Object method() {
  ...
}

@Cacheable(key = "args[0]", duration = 10, timeUnit = TimeUnit.MINUTES)
public Object method(Object arg0, Object arg1) {
  ...
}

```

### Jdbc

- 可单独使用，也可配合oxygen-core进行自动配置

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


## 联系信息

E-mail: qq11419041@163.com

QQ: 1106088328