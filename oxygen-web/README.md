# oxygen-web

轻量级web实现


## 介绍

一个轻量级web实现

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
  │  │- mapping  //url映射，参数映射相关注解和实体
  │  │- router  //一个示例路由（获取服务器时间）
  │  │- view  //视图解析
  │  │- DefaultWebAppInitializer.java  //默认初始化实现
  │  │- DispatcherServlet.java  //路由分发器
  │  │- WebAppInitializer.java  //web自动初始化接口，提供给用户自定义使用
  │  │- WebContainerInitializer.java  //容器自动初始化
  │  └─ WebPlugin.java  //web插件
  └─ resources/META-INF/services
      │- ...ServletContainerIntializer //servlet3.0规范
      │- ...core.Plugin  //增加web插件
      │- ...ParamHandler //参数处理服务
      │- ...RequestParse //请求解析服务
      └─ ...ViewResolver //视图解析服务
```

## 特性

* 轻量级，注释完善，使用简单
* 使用ServiceLoader加载插件，易于扩展


## 安装

添加依赖到你的 pom.xml:
```
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-web</artifactId>
    <version>${oxygen.version}</version>
</dependency>

```

## 快速开始

### 基础使用
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

### 自定义视图解析
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

### 添加web启动执行类
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


### 使用内置容器启动
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

