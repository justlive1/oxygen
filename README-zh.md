# oxygen
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/vip.justlive/oxygen/badge.svg)](https://maven-badges.herokuapp.com/maven-central/vip.justlive/oxygen/)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)


轻量级Java框架



## 介绍

一个轻量级Java框架

- oxygen-core
  - 配置管理，支持${attrs.key:defaultValue}表达式获取配置
  - 加解密管理，提供加解密服务内置基础加密实现，例如SHA-1、SHA-256、MD5
  - 异常管理，提供异常包装，统一异常编码，便于国际化
  - i18n国际化
  - 资源文件加载，提供file,jar,classpath等文件加载
  - 类扫描器
  - 部分工具类
  - retry重试

- oxygen-ioc
  - 基于构造器的轻量级依赖注入

- oxygen-aop
  - 基于cglib实现的切面
  - 轻巧简单，可单独使用
  - 可使用注解`Aspect`或直接实现`Interceptor`编写切面

- oxygen-cache
  - 内置Ehcache和LocalCache缓存
  - 提供基于注解`Cacheable`的方法缓存

- oxygen-job
  - 提供基于注解`Scheduled`的定时任务

- oxygen-jdbc
  - 小巧简单的jdbc实现，纯jdk实现，无第三方jar
  - 支持多数据源
  - 基于sql进行crud，不提供类似Hibernate的链式方法（原因：sql作为数据库领域的DSL，已经很自然优雅，Less is more）

- oxygen-web
  - 轻量级web框架支持注解声明和函数式编程
  - 支持Servlet3.0 `ServletContainerInitializer` 自动加载，省略web.xml
  - 支持i18n动态切换
  - 提供`WebHook`进行请求拦截处理
  - 支持自定义全局异常处理
  


## 特性

* 轻量级，使用简单
* 支持插件扩展
* 函数式编程
* 流式风格


## 快速开始

创建`Maven`项目

```xml
<!-- 使用内嵌tomcat启动 -->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-web-tomcat</artifactId>
    <version>${oxygen.version}</version>
</dependency>
```

或`Gradle`

```
compile 'vip.justlive:oxygen-web-tomcat:$oxygenVersion'
```

> 不需要`webapp`项目框架，支持Servlet3.0

编写 `main` 函数写一个 `Hello World`

```java
public static void main(String[] args) {
  Router.router().path("/").handler(ctx -> ctx.response().write("hello world"));
  Server.server().listen(8080);
}
```

用浏览器打开 http://localhost:8080 这样就可以看到 `hello world` 了！

## 内容详解
- [**`注册路由`**](#注册路由)
  - [**`硬编码方式`**](#硬编码方式)
  - [**`注解方式`**](#注解方式)
- [**`获取请求参数`**](#获取请求参数)
  - [**`表单参数或json请求参数`**](#表单参数或json请求参数)
  - [**`restful参数`**](#restful参数)
  - [**`header参数`**](#header参数)
  - [**`cookie参数`**](#cookie参数)
  - [**`参数转对象`**](#参数转对象)
- [**`静态资源`**](#静态资源)
- [**`上传文件`**](#上传文件)
- [**`结果渲染`**](#结果渲染)
  - [**`渲染json`**](#渲染json)
  - [**`渲染文本`**](#渲染文本)
  - [**`渲染html`**](#渲染html)
  - [**`渲染模板`**](#渲染模板)
  - [**`重定向`**](#重定向)
  - [**`写入cookie`**](#写入cookie)
  - [**`添加header`**](#添加header)
  - [**`写入session`**](#写入session)
- [**`拦截器`**](#拦截器)
- [**`异常处理`**](#异常处理)
- [**`部署项目`**](#部署项目)
  - [**`修改端口`**](#修改端口)
  - [**`运行项目`**](#运行项目)
      
### 注册路由
#### 硬编码方式
```java
Router.router().path("/").handler(ctx -> ctx.response().write("hello world"));
Router.router().path("/get").method(HttpMethod.GET).handler(get);
Router.router().path("/post").method(HttpMethod.POST).handler(post);
```

#### 注解方式
```java
@Router("/book")
public class BookRouter {
  
  // 视图
  @Mapping("/")
  public ViewResult index() {
    return Result.view("/book.html");
  }
  
  // json
  @Mapping(value = "/ajax", method = {HttpMethod.POST})
  public Book find(RoutingContext ctx) {
    // ...
    return new Book();
  }
}
```

### 获取请求参数
#### 表单参数或json请求参数

项目将json请求参数与表单参数合并，使用相同的方法或注解获取

**使用RoutingContext获取**
```java
Router.router().path("/").handler(ctx -> {
  String id = ctx.request().getParam("id");
  ctx.response().write(id);
});
```
**使用注解获取**
```java
@Mapping(value = "/ajax", method = {HttpMethod.POST})
public Book find(@Param Long id, @Param("tp") String type) {
  // ...
  return new Book();
}
```

#### restful参数

**使用RoutingContext获取**
```java
Router.router().path("/{id}").handler(ctx -> {
  String id = ctx.request().getPathVariable("id");
  ctx.response().write(id);
});
```
**使用注解获取**
```java
@Mapping(value = "/{id}", method = {HttpMethod.POST})
public void ajax(@PathParam("id") Long id) {
  // ...
}
```

#### header参数

**使用RoutingContext获取**
```java
Router.router().path("/").handler(ctx -> {
  String id = ctx.request().getHeader("id");
  ctx.response().write(id);
});
```
**使用注解获取**
```java
@Mapping(value = "/", method = {HttpMethod.POST})
public void ajax(@HeaderParam("id") Long id) {
  // ...
}
```

#### cookie参数

**使用RoutingContext获取**
```java
Router.router().path("/").handler(ctx -> {
  String id = ctx.request().getCookie("id");
  ctx.response().write(id);
});
```
**使用注解获取**
```java
@Mapping(value = "/", method = {HttpMethod.POST})
public void ajax(@CookieParam("id") Long id) {
  // ...
}
```

#### 参数转对象

**实体类**
```java
@Data
public class Book {
  private String name;
  private String author;
}
```

**使用RoutingContext转换**
```java
Router.router().path("/").handler(ctx -> {
  // 表单或json请求参数绑定
  Book book = ctx.bindParam(Book.class);
  // cookie参数绑定
  book = ctx.bindCookie(Book.class);
  // header参数绑定
  book = ctx.bindHeader(Book.class);
  // restful参数绑定
  book = ctx.bindPathVariables(Book.class);
});
```
**使用注解获取**
```java
@Mapping(value = "/", method = {HttpMethod.POST})
public void ajax(@Param Book b1, @CookieParam Book b2, @HeaderParam Book b3, @PathParam Book b4) {
  // ...
}
```

### 静态资源

内置默认将`classpath`下`/public,/static`作为静态资源目录，支持`webjars`，映射到`/public`

自定义静态资源可使用下面代码
```java
Router.staticRoute().prefix("/lib").location("classpath:lib");
```
也可以通过配置文件指定
```properties
web.static.prefix=/public
web.static.path=/public,/static,classpath:/META-INF/resources/webjars
```

### 上传文件

**使用RoutingContext获取**
```java
Router.router().path("/").handler(ctx -> {
  MultipartItem file = ctx.request().getMultipartItem("file");
  // ...
});
```
**使用注解获取**
```java
@Mapping("/")
public void upload(MultipartItem image, @MultipartParam("file1") MultipartItem file) {
  // 不使用注解则使用方法参数名作为请求参数名称
  // 使用注解指定请求参数名称
}
```

### 结果渲染

#### 渲染json
```java
// 使用RoutingContext返回
Router.router().path("/").handler(ctx -> {
  ctx.response().json(new Book("Java", "xxx"));
});

// 注解式
@Mapping("/")
public Book find() {
  // 直接返回对象，框架默认处理成json
  return new Book("Java", "xxx");
}
```

#### 渲染文本
```java
// 使用RoutingContext返回
Router.router().path("/").handler(ctx -> {
  ctx.response().text("hello world");
});
```

#### 渲染html
```java
// 使用RoutingContext返回
Router.router().path("/").handler(ctx -> {
  ctx.response().html("<html><body><span>hello world</span></body></html>");
});
```

#### 渲染模板
内置支持了`jsp`和`thymeleaf`模板，默认对应`resources`下的`WEB-INF`和`templates`目录
```properties
# 可通过下面配置进行更改模板目录
web.view.jsp.prefix=WEB-INF
web.view.thymeleaf.prefix=/templates
```
模板使用
```java
// 使用RoutingContext
Router.router().path("/").handler(ctx -> {
  ctx.response().template("index.html");
});

Router.router().path("/").handler(ctx -> {
  Map<String, Object> attrs = new HashMap<>();
  // ...
  ctx.response().template("index.html", attrs);
});

// 注解式
@Mapping("/")
public Result index() {
  return Result.view("index.html");
}

@Mapping("/")
public Result index() {
  Map<String, Object> attrs = new HashMap<>();
  // ...
  return Result.view("index.html").addAttributes(attrs);
}
```

#### 重定向

```java
Router.router().path("/").handler(ctx -> {
  ctx.response().redirect("https://github.com/justlive1");
});

@Mapping("/a")
public Result index() {
  // 内部地址 相对于根目录: /b
  // return Result.redirect("/b"); 
  // 内部地址 相对于当前路径: /a/b
  // return Result.redirect("b");
  // 协议地址
  return Result.redirect("https://github.com/justlive1");
}
```

#### 写入cookie

```java
@Mapping("/")
public void index(RoutingContext ctx) {
  ctx.response().setCookie("hello", "world");
  ctx.response().setCookie("java", "script", 100);
  ctx.response().setCookie("uid", "xxx", ".justlive.vip", "/", 3600, true);
}
```

#### 添加header

```java
@Mapping("/")
public void index(RoutingContext ctx) {
  ctx.response().setHeader("hello", "world");
}
```

#### 写入session

```java
@Mapping("/")
public void index(RoutingContext ctx) {
  ctx.request().getSession().put("key", "value");
}
```

### 拦截器

`WebHook`是拦截器接口，可以实现执行前、执行后和结束拦截处理

```java
@Slf4j
@Bean
public class LogWebHook implements WebHook {
  @Override
  public boolean before(RoutingContext ctx) {
    log.info("before");
    return true;
  }
  @Override
  public void after(RoutingContext ctx) {
    log.info("after");
  }
  @Override
  public void finished(RoutingContext ctx) {
    log.info("finished");
  }
}
```

### 异常处理

框架默认提供了一个异常处理器，如需自定义处理异常，可以像下面这样使用

```java
@Bean
public class CustomExceptionHandler extends ExceptionHandlerImpl {

  @Override
  public void handle(RoutingContext ctx, Exception e, int status) {
    if (e instanceof CustomException) {
      // do something
    } else {
      super.handle(ctx, e, status);
    }
  }
}
```

### 部署项目

#### 修改端口

**编码指定**
```java
Server.server().listen(8080);
```
**配置文件**
```properties
server.port=8081
```

#### 运行项目

**使用内嵌容器启动**

启动类
```java
public class Application {
  public static void main(String[] args) {
    Server.server().listen();
  }
}
```
通用打包方式
- `${mainClass}`为上面的启动类
- 会生成`lib`目录存放依赖`jar`
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <source>${maven.compiler.source}</source>
        <target>${maven.compiler.target}</target>
        <encoding>UTF-8</encoding>
      </configuration>
    </plugin>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-jar-plugin</artifactId>
      <configuration>
        <archive>
          <manifest>
            <addClasspath>true</addClasspath>
            <classpathPrefix>lib/</classpathPrefix>
            <mainClass>${mainClass}</mainClass>
          </manifest>
        </archive>
      </configuration>
    </plugin>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-dependency-plugin</artifactId>
      <executions>
        <execution>
          <id>copy</id>
          <phase>package</phase>
          <goals>
            <goal>copy-dependencies</goal>
          </goals>
          <configuration>
            <outputDirectory>${project.build.directory}/lib</outputDirectory>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

打成`fat-jar`:
- 使用springboot打包插件
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <version>1.3.8.RELEASE</version>
      <executions>
        <execution>
          <phase>package</phase>
          <goals>
            <goal>repackage</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

**使用外部容器（jetty、tomcat等）**

无需web.xml配置，打包成`war`放入容器即可，实现机制可查看`WebContainerInitializer`

```xml
<!-- 解决默认打war包报错 webxml attribute is required -->
<properties>
  <failOnMissingWebXml>false</failOnMissingWebXml>
</properties>
```

### 外部化配置

框架可以通过使用配置文件进行修改默认属性
```properties
##### 基础配置
# 配置覆盖地址，用户外部配置覆盖项目配置 例 file:/config/*.properties,classpath*:/config/*.properties,xx.properties
config.override.path=
# 类扫描路径属性
main.class.scan=vip.justlive
# 临时文件根目录
main.temp.dir=.oxygen
# 缓存实现类，自定义缓存时使用
cache.impl.class=


##### web 
# embedded 启动端口
server.port=8080
# context path
server.contextPath=
# session失效时间，单位秒
web.session.expired=3600
# 默认静态资源请求前缀
web.static.prefix=/public
# 默认静态资源目录
web.static.path=/public,/static,classpath:/META-INF/resources/webjars
# 静态资源缓存时间
web.static.cache=3600
# jsp路径前缀
web.view.jsp.prefix=WEB-INF
# thymeleaf 路径前缀
web.view.thymeleaf.prefix=/templates
# 是否开启模板缓存
web.view.cache.enabled=true


##### 定时任务job
# job线程名称格式
job.thread.name.format=jobs-%d
# job核心线程池大小
job.core.pool.size=10


##### i18n国际化
# i18n配置文件地址
i18n.path=classpath:message/*.properties
# i18n默认语言
i18n.default.language=zh
# i18n默认国家
i18n.default.country=CN
# i18n参数key
i18n.param.key=locale
# i18n Session key
i18n.session.key=I18N_SESSION_LOCALE
```

## 联系信息

E-mail: qq11419041@163.com

QQ群: 950216299