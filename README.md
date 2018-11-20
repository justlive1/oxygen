# oxygen

light framework


## Documentation

a light framework base on Java

oxygen-core
- aop base on cglib
- provide cache manager
- config manager, support `${attrs.key:defaultValue}`
- crypto
- exception manager, i18n friendly
- ioc
- scheduled job

```
├─ src/main
  │─ java/.../core  //oxygen-core 
  │  │- aop //aop module
  │  │- cache //cache module
  │  │- config  //config module
  │  │- constant  //constant
  │  │- convert  //type convert module
  │  │- crypto  //crypto module
  │  │- domain  //domain module
  │  │- exception  //exception module
  │  │- io  //io module
  │  │- ioc  //ioc module
  │  │- job  //scheduled job module
  │  │- scan  //class scan module
  │  │- util  //util module
  │  │- Bootstrap.java  //the class to start framework
  │  └- Plugin.java   //the interface of plugin
  └─ resources/META-INF/services
     └- ...core.Plugin  //Plugin service configuration file
  
```

oxygen-jdbc
- simple only base on jdk
- support multi-datasource
- base on `sql` (sql is the DSL of database. It is very natural and elegant. Less is more)

```
├─ src/main
  │─ java/.../jdbc  //oxygen-jdbc
  │  │- config  //datasource config 
  │  │- handler  //data handler
  │  │- interceptor  //jdbc execute interceptor
  │  │- record  //base crud
  │  │- Jdbc.java  //the class used to operate database
  │  │- JdbcException.java  //jdbc excption
  │  └- JdbcPlugin.java   //jdbc plugin
  └─ resources/META-INF/services
     │- ...handler.ColumnHandler //columnHandler service configuration file
     └- ...core.Plugin  //add jdbcPlugin
  
```

oxygen-web
* use`ServletContainerInitializer` to auto startup
* no require the file `web.xml`
* use `@Router` to mark a router class
* use `@Mapping` to mark a request handler method
* use `@Param,@HeaderParam,@CookieParam,@PathParam` to mark the parameter where to fetch, `@Param` is default
* supported simple types , `Map<String,Object>` and object class user defined
* supported return json , view and anything handled by yourself

```
├─ src/main
  │─ java/.../web 
  │  │- handler // parameter hander
  │  │- http // http parse
  │  │- mapping  // url mapping and paratemer mapping
  │  │- router  // an example
  │  │- server  // embedded server
  │  │- view  // view resolver
  │  │- DefaultWebAppInitializer.java  // default web app initializer 
  │  │- DispatcherServlet.java  // dispathcer
  │  │- WebAppInitializer.java  // interface of web app initializer
  │  │- WebContainerInitializer.java  // support of web container initializer
  │  │- WebConf.java  // web config properties
  │  └- WebPlugin.java  // web plugin
  └─ resources/META-INF/services
      │- ...ServletContainerIntializer // servlet3.0
      │- ...core.Plugin  // add web plugin
      │- ...ParamHandler // add paramhander services
      │- ...RequestParse // add request parser services 
      └- ...ViewResolver // add view resolver services
```


## Features

* light and simple to use
* use `ServiceLoader` to load plugins and easy to extend

## Install

Add dependencies to your pom.xml:
```
<!-- core, include ioc, aop, config manager, crypto encoder, exceptions, scheduled job, cache and so on-->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-core</artifactId>
    <version>${oxygen.version}</version>
</dependency>

<!-- jdbc, can be used alone -->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-jdbc</artifactId>
    <version>${oxygen.version}</version>
</dependency>

<!-- web -->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-web</artifactId>
    <version>${oxygen.version}</version>
</dependency>

<!-- web with embedded tomcat-->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-web-tomcat</artifactId>
    <version>${oxygen.version}</version>
</dependency>

```

## Quick start

### Common Response

we use `Resp` as response

```
// success response with code 00000
Resp.success(Object obj);

// error response with default code 99999
Resp.error(String msg);

// error response with custom code
Resp.error(String code, String msg);
```

### Exceptions

we use `Exceptions` to throw a runtime exception

```
// create instance of ErrorCode
ErrorCode err = Exceptions.errorCode(String module, String code);
ErrorCode err = Exceptions.errorMessage(String module, String code, String message);

// throw an unchecked exception wrapped
throw Exceptions.wrap(Throwable e);
throw Exceptions.wrap(Throwable e, String code, String message);
throw Exceptions.wrap(Throwable e, ErrorCode errorCode, Object... arguments);

// throw a business exception with none stack trace
throw Exceptions.fail(ErrorCode errCode, Object... params);
throw Exceptions.fail(String code, String message, Object... params);

// throw a fault with stack trace
throw Exceptions.fault(ErrorCode errCode, Object... params);
throw Exceptions.fault(String code, String message, Object... params);
throw Exceptions.fault(Throwable e, ErrorCode errCode, Object... params);
throw Exceptions.fault(Throwable e, String code, String message, Object... params)

```


### Retry

retry, support sync and async

```
RetryBuilder.newBuilder()
  // time limit
  .withTimeLimit(10, TimeUnit.MILLISECONDS)
  // retry if throw exception
  .retryIfException()
  // set max attempt numbers
  .withMaxAttempt(3)
  // block use sleep
  .withSleepBlock(100)
  // build sync retryer
  .build()
  // execute
  .call(Math::random);
  
RetryBuilder.newBuilder()
    // retry if throw ArithmeticException 
    .retryIfException(ArithmeticException.class)
    // retry if result < 0.5
    .retryIfResult(r -> r < 0.5)
    // set max delay time
    .withMaxDelay(1000)
    // block use wait
    .withWaitBlock(100)
    // build sync retryer
    .build()
    // execute
    .call(Math::random);

RetryBuilder.newBuilder()
    // retry by yourslef
    .retryIf(attmapt -> attmapt.hasException())
    // listern of retry
    .onRetry(System.out::println)
    // listern of final fail
    .onFinalFail(System.out::println)
    // listern of success
    .onSuccess(System.out::println)
    // set user async executor
    .withAsyncExecutor(scheduleService)
    // build async retryer
    .buildAsync()
    // execute
    .callAsync(Math::random);
```

### IOC 

you can use IOC container with annotation

```
// add scan packages property in config file
main.class.scan=com.xxx.xxx,com.aaa.bbb

// use @Configuration and @Bean
@Configuration
public class Conf {
 
  @Bean
  Inter noDepBean() {
    return new NoDepBean();
  }
}

// use @Bean and @Inject
@Bean("depBean")
public class DepBean implements Inter {

  private final NoDepBean noDepBean;

  @Inject
  public DepBean(NoDepBean noDepBean) {
    this.noDepBean = noDepBean;
  }
  
  ...
}

// get bean at runtime
Inter inter = BeanStore.getBean("depBean", Inter.class);

```

### AOP

we can use aop with annotations

```
// define aop method
@Before(annotation = Log.class)
public void log(Invocation invocation) {
  ...
}

// target method
@Log
public void print() {
  ...
}  
```


### Scheduled Job

`@Scheduled` that marks a method to be scheduled.

Exactly one of the onApplicationStart(), cron(), fixedDelay(), or fixedRate() attributes must be specified.

```
// Creates and executes a periodic action that becomes enabled first after the given initial delay, 
// and subsequently with the given delay between the termination of one execution and the commencement of the next.
@Scheduled(fixedDelay = "500")
public void run1() {
  ...
}

// Creates and executes a periodic action that becomes enabled first after the given initial delay, 
// and subsequently with the given period
@Scheduled(fixedRate = "600")
public void run2() {
  ...
}

// Schedule the specified cron task and run in async mode when application started 
@Scheduled(cron = "0/5 * * * * ?", onApplicationStart = true, async = true)
public void run3() {
  ...
}
```


### Cache

There are two ways to use the cache
- `JCache.cache()` Get the cache and then call the api
- `@Cacheable` Use annotation to add cache on method

```
// use cache api 
Cache cache = JCache.cache(cacheName);
T value = cache.get(key, clazz);
cache.set(key, value, duration, timeUnit);
...

// use annotation
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

#### basic

- multi-datasource
- crud using `Jdbc` 
- handler ResultSet by yourself using `ResultHandler<T>`
- start and close transaction

```
// use it alone
...
// add primary datasource
Jdbc.addPrimaryDataSource(DataSource dataSource)
// add mutli datasource
Jdbc.addDataSource(String name, DataSource dataSource)

// crud
T Jdbc.query(String sql, Class<T> clazz, Object... params)
List<T> Jdbc.queryForList(String sql, Class<T> clazz, Object... params)
Map<String, Object> Jdbc.queryForMap(String sql, Object... params)
List<Map<String, Object>> Jdbc.queryForMapList(String sql, Object... params)
// you can handler resultset by yourself
T Jdbc.query(String sql, ResultSetHandler<T> handler, Object... params)

int Jdbc.update(String sql, Object... params)

// start primary datasource transaction
Jdbc.startTx()
// start named datasource transaction
Jdbc.startTx(String dataSourceName)

// close primary datasource transaction
Jdbc.closeTx()
// close named datasource transaction
Jdbc.closeTx(String dataSourceName)



// roback
Jdbc.rollbackTx()
// roback 
Jdbc.rollbackTx(String dataSourceName)

// base crud
Option opt = new Option()
...
Record.insert(opt)
Record.findById(Option.class, 1)
Record.find(opt)
Record.update(opt)
Record.deleteById(Option.class, 1)
Record.delete(opt);


// use oxygen-core, only need to write configuration file
// multi datasource names
datasource.multi=a
// primary datasource
datasource.logSql=true
datasource.driverClassName=org.h2.Driver
datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1
datasource.username=sa
datasource.password=sa

// datasource named by a
datasource.a.driverClassName=org.h2.Driver
datasource.a.url=jdbc:h2:mem:a;DB_CLOSE_DELAY=-1
datasource.a.username=sa
datasource.a.password=sa

```


#### handler column yourself

- implement `ColumnHandler`
- add `META-INF/services/vip.justlive.oxygen.jdbc.handler.ColumnHandler` file and add the class name

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

// add or update META-INF/services/vip.justlive.oxygen.jdbc.handler.ColumnHandler file and add class name
xxx.xxx.MyColumnHandler

```

#### add jdbc interceptor

- implement `JdbcInterceptor` 
- add interceptor using `Jdbc.addJdbcInterceptor` 

```
// embed print sql interceptor
@Slf4j
public class LogSqlJdbcInterceptor implements JdbcInterceptor {

  @Override
  public void before(String sql, List<Object> params) {
    if (log.isDebugEnabled()) {
      log.debug("execute sql: {} -> params: {}", sql, params);
    }
  }
}

// add interceptor
Jdbc.addJdbcInterceptor(JdbcInterceptor interceptor)

```


### web

#### 基础使用
- use `@Router @Mapping @Param...` to mark router class, request handle method and parameter
- use `View` to render or redirect views，return json when the return type is not `void` or `View`
- use `Request.current(),Response.current()` to get request or response in one thread

```
// use @Router
@Router("/common")
public class CommonRouter {

  // mark request path and request method, default is all
  @Mapping(value = "/localDate",method = HttpMethod.GET)
  public Resp localDate() {
    return Resp.success(
            LocalDate.now().plusDays(offset).atStartOfDay(ZoneOffset.systemDefault()).toInstant()
                .toEpochMilli());
  }

  // view render
  @Mapping("/index")
  public View index() {
    View view = new View();
    view.setPath("/index.jsp");
    return view;
  }
  
  // view redirect
  @Mapping("/view")
  public View index() {
    View view = new View();
    view.setPath("/index");
    view.setRedirect(true);
    return view;
  }
  
  // handle by yourself, the example is download
  @Mapping("download")
  public void download(HttpServletResponse resp) throws IOException {
    resp.setCharacterEncoding("utf-8");
    resp.setContentType("application/octet-stream;charset=utf-8");
    resp.setHeader("Content-disposition", "attachment;filename=xx.txt");
    Files.copy(new File("xxx.txt"), resp.getOutputStream());
  }
}

// get Request Response
Request.current()
Response.current()


// redirect page for 404 error
web.error.404.page=
// handler by yourself for 404 error
web.error.404.handler=
// redirect page for 500 error
web.error.500.page=
// handler by yourself for 404 error
web.error.500.handler=

```


#### add your ViewResolver
- implement`ViewResolver`
- use`ServiceLoader` or `WebPlugin.addViewResolver` to add

```
// an example in system
public class DefaultViewResolver implements ViewResolver {

  @Override
  public boolean supported(Object data) {
    // redirect
    if (data != null && data.getClass() == View.class && ((View) data).isRedirect()) {
      return true;
    }
    // null or not view, return json
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

#### add your WebAppInitializer
only need implement`WebAppInitializer` and the system can auto load

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


#### run with embedded server
- depend `oxygen-web-tomcat`
- run application using `Server.start`

```
<!-- web wiht embeded tomcat -->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-web-tomcat</artifactId>
    <version>${oxygen.version}</version>
</dependency>


public static void main(String[] args) {
  // start server
  Server.start();
  ...
  // stop server
  Server.stop();
}
```



## Contact information

E-mail: qq11419041@163.com

QQ: 1106088328