# oxygen
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/vip.justlive/oxygen/badge.svg)](https://maven-badges.herokuapp.com/maven-central/vip.justlive/oxygen/)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)


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
  │  │- i18n  //i18n
  │  │- io  //io读写目录
  │  │- ioc  //ioc实现目录
  │  │- job  //定时任务实现目录
  │  │- scan  //类扫描实现目录
  │  │- util  //工具类目录
  │  │- Bootstrap.java  //框架启动引导类
  │  └- Plugin.java   //插件接口
  └─ resources/META-INF/services
     └- ...core.Plugin  //Plugin服务实现配置文件
  
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
  │  │- record  //基础crud
  │  │- Jdbc.java  //Jdbc核心操作类，提供crud操作
  │  │- JdbcException.java  //jdbc异常封装
  │  └- JdbcPlugin.java   //jdbc插件，与oxygen-core配套使用
  └─ resources/META-INF/services
     │- ...handler.ColumnHandler //列处理服务配置文件
     └- ...core.Plugin  //增加jdbcPlugin服务实现，与oxygen-core配套使用
  
```


oxygen-web
* 项目使用了Servlet3.0的`ServletContainerInitializer`接口
* 在servlet中可自动加载，不需要在web.xml中配置
* 使用`@Router`标记路由文件
* 使用`@Mapping`标记请求路径处理方法
* 参数绑定的取值域使用`@Param,@HeaderParam,@CookieParam,@PathParam`指定，默认为`@Param`
* 参数绑定支持简单类型 + Map<String,Object> + 用户实体类
* 支持返回Json、视图或自定义实现(文件下载等)

```
├─ src/main
  │─ java/.../web  //oxygen-web代码目录
  │  │- handler //参数绑定处理
  │  │- http //http请求解析
  │  │- i18n  //i18n切面
  │  │- mapping  //url映射，参数映射相关注解和实体
  │  │- router  //一个示例路由（获取服务器时间）
  │  │- server  //内置server接口和启动类
  │  │- view  //视图解析
  │  │- DefaultWebAppInitializer.java  //默认初始化实现
  │  │- DispatcherServlet.java  //路由分发器
  │  │- WebAppInitializer.java  //web自动初始化接口，提供给用户自定义使用
  │  │- WebContainerInitializer.java  //容器自动初始化
  │  │- WebConf.java  //web配置
  │  └- WebPlugin.java  //web插件
  └─ resources/META-INF/services
      │- ...ServletContainerIntializer //servlet3.0规范
      │- ...core.Plugin  //增加web插件
      │- ...ParamHandler //参数处理服务
      │- ...RequestParse //请求解析服务
      └- ...ViewResolver //视图解析服务
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

<!-- web实现 已依赖了core -->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-web</artifactId>
    <version>${oxygen.version}</version>
</dependency>

<!-- 已依赖了web 并提供了embeded tomcat -->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-web-tomcat</artifactId>
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

### Retry

重试，支持同步/异步

```
RetryBuilder.newBuilder()
  // 超时限制
  .withTimeLimit(10, TimeUnit.MILLISECONDS)
  // 抛出异常就重试 
  .retryIfException()
  // 最大尝试次数
  .withMaxAttempt(3)
  // 使用sleep进行阻塞
  .withSleepBlock(100)
  // 构造同步重试器
  .build()
  // 执行逻辑
  .call(Math::random);
  
RetryBuilder.newBuilder()
    // 抛出ArithmeticException异常就重试 
    .retryIfException(ArithmeticException.class)
    // 结果小于0.5就重试
    .retryIfResult(r -> r < 0.5)
    // 最大延时时间
    .withMaxDelay(1000)
    // 使用wait进行阻塞
    .withWaitBlock(100)
    // 构造同步重试器
    .build()
    // 执行逻辑
    .call(Math::random);

RetryBuilder.newBuilder()
    // 自定义重试判断
    .retryIf(attmapt -> attmapt.hasException())
    // 重试监听
    .onRetry(System.out::println)
    // 最终失败监听
    .onFinalFail(System.out::println)
    // 成功监听
    .onSuccess(System.out::println)
    // 设置用户线程池
    .withAsyncExecutor(scheduleService)
    // 构造异步重试器
    .buildAsync()
    // 执行异步逻辑
    .callAsync(Math::random);
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


### 国际化

使用 `Lang` 获取国际化信息
```
// 配置国际化文件路径
i18n.path=classpath:message/*.properties
// i18n默认国家
i18n.default.language=zh
// i18n默认国家
i18n.default.country=CN

// 设置当前线程国际化
Lang.setThreadLocale(new Locale("en", "US"))
// 还原当前线程国际化
Lang.clearThreadLocale()
// 获取默认locale的国际化信息
Lang.getMessage("key")
// 获取指定locale的国际化信息
Lang.getMessage("key", new Locale("en", "US")
Lang.getMessage("key", "en", "US")

```

### Jdbc

#### 基础使用

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

// 切换当前线程的数据源
Jdbc.use(dataSourceName)
// 还原当前线程的数据源
Jdbc.clear()

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


// 回滚事务
Jdbc.rollbackTx()
// 回滚指定数据源的事务
Jdbc.rollbackTx(String dataSourceName)

// 基础crud
Option opt = new Option()
...
Record.insert(opt)
Record.findById(Option.class, 1)
Record.find(opt)
Record.update(opt)
Record.deleteById(Option.class, 1)
Record.delete(opt);

// 批处理
// 开启批处理
Batch.use()
// 添加预处理sql
.addBatch("insert into system (key, value) values (?,?)", 1, 3)
.addBatch("insert into system (key, value) values (?,?)", Arrays.asList(4, 4))
// 添加直接处理sql
.addBatch("insert into system (key,value) values(1,2)")
// 提交批处理
.commit();
// 批量插入
Record.insertBatch(list);

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

#### 自定义列类型转换

- 实现 `ColumnHandler` 接口进行自定义处理
- 增加 `META-INF/services/vip.justlive.oxygen.jdbc.handler.ColumnHandler` 文件，添加实现类的类名

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

#### 增加jdbc拦截

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

### web

#### 基础使用
- 使用`@Router @Mapping @Param...`等注解进行定义路由类和请求方法以及绑定参数
- 使用`View`进行视图跳转，非`void,View`的返回值默认使用json处理
- 线程内使用`Request.current(),Response.current()`获取请求和返回

```
// 使用 @Router标记路由
@Router("/common")
public class CommonRouter {

  // 标记请求路径和请求方式，默认支持所有请求 当返回值不是void且非View则为返回json
  @Mapping(value = "/localDate",method = HttpMethod.GET)
  public Resp localDate() {
    return Resp.success(
            LocalDate.now().plusDays(offset).atStartOfDay(ZoneOffset.systemDefault()).toInstant()
                .toEpochMilli());
  }

  // 页面渲染 需要返回View
  @Mapping("/index")
  public View index() {
    View view = new View();
    view.setPath("/index.jsp");
    return view;
  }
  
  // 重定向
  @Mapping("/view")
  public View index() {
    View view = new View();
    // 相对路径为容器内跳转 使用http://xxx则绝对跳转
    view.setPath("/index");
    view.setRedirect(true);
    return view;
  }
  
  // 文件下载
  @Mapping("download")
  public void download(HttpServletResponse resp) throws IOException {
    resp.setCharacterEncoding("utf-8");
    resp.setContentType("application/octet-stream;charset=utf-8");
    resp.setHeader("Content-disposition", "attachment;filename=xx.txt");
    Files.copy(new File("xxx.txt"), resp.getOutputStream());
  }
}

// 获取当前线程的Request Response
Request.current()
Response.current()

// 配置404错误页面跳转
web.error.404.page=
// 配置404自定义处理
web.error.404.handler=
// 配置500错误页面跳转
web.error.500.page=
// 配置500自定义处理
web.error.500.handler=

```


#### 自定义视图解析
- 实现`ViewResolver`接口
- 通过`ServiceLoader`或`WebPlugin.addViewResolver`的方式进行添加自定义视图解析

```
// 内置的重定向和json解析器
public class DefaultViewResolver implements ViewResolver {

  @Override
  public boolean supported(Object data) {
    // 重定向
    if (data != null && data.getClass() == View.class && ((View) data).isRedirect()) {
      return true;
    }
    // null 或者 非view 返回json
    return data == null || data.getClass() != View.class;
  }

  @Override
  public void resolveView(HttpServletRequest request, HttpServletResponse response, Object data) {
    try {
      if (data != null && data.getClass() == View.class) {
        View view = (View) data;
        String redirectUrl = view.getPath();
        if (!redirectUrl.startsWith(Constants.HTTP_PREFIX) && !redirectUrl
            .startsWith(Constants.HTTPS_PREFIX)) {
          redirectUrl = request.getContextPath() + view.getPath();
        }
        response.sendRedirect(redirectUrl);
      } else {
        response.getWriter().print(JSON.toJSONString(data));
      }
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
```

#### 添加web启动执行类
只需实现`WebAppInitializer`接口，容器会自动加载

```
public class MyWebAppInitializer implements WebAppInitializer {
  @Override
  public void onStartup(ServletContext context) {
    ...
  }
  @Override
  public int order() {
    ...
  }
}
```

#### 国际化
内置提供了切面`I18nAop`用于动态切换语言

```
// 配置国际化文件路径
i18n.path=classpath:message/*.properties
// i18n默认国家
i18n.default.language=zh
// i18n默认国家
i18n.default.country=CN
// 设置locale的请求入参
i18n.param.key=locale
// session 保存的key
i18n.session.key=I18N_SESSION_LOCALE

// 动态切换例子
// 默认locale
http://localhost:8080/page.html
// 指定locale为zh_CN
http://localhost:8080/page1.html?locale=zh_CN
// 相同session下 locale为设置的zh_CN
http://localhost:8080/page2.html
```

#### 使用内置容器启动
- 依赖 `oxygen-web-tomcat`
- 在main中使用 `Server.start`启动

```
<!-- 已依赖了web 并提供了embeded tomcat -->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-web-tomcat</artifactId>
    <version>${oxygen.version}</version>
</dependency>


public static void main(String[] args) {
  // 启动容器
  Server.start();
  ...
  // 关闭容器
  Server.stop();
}
```


## 联系信息

E-mail: qq11419041@163.com

QQ群: 950216299