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
  │  └─ Plugin.java   //the interface of plugin
  └─ resources/META-INF/services
     └─ ...core.Plugin  //Plugin service configuration file
  
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
  │  │- Jdbc.java  //the class used to operate database
  │  │- JdbcException.java  //jdbc excption
  │  └─ JdbcPlugin.java   //jdbc plugin
  └─ resources/META-INF/services
     │- ...handler.ColumnHandler //columnHandler service configuration file
     └─ ...core.Plugin  //add jdbcPlugin
  
```


## Features

* light and simple to use
* user `ServiceLoader` to load plugins and easy to extend

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



## Contact information

E-mail: qq11419041@163.com

QQ: 1106088328