/*
 * Copyright (C) 2018 justlive1
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
package vip.justlive.oxygen.web;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.ioc.BeanStore;
import vip.justlive.oxygen.core.ioc.IocPlugin;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;
import vip.justlive.oxygen.core.util.Checks;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.web.handler.DefaultErrorHandler;
import vip.justlive.oxygen.web.handler.ErrorHandler;
import vip.justlive.oxygen.web.handler.ParamHandler;
import vip.justlive.oxygen.web.http.RequestParse;
import vip.justlive.oxygen.web.mapping.Action;
import vip.justlive.oxygen.web.mapping.DataBinder;
import vip.justlive.oxygen.web.mapping.Mapping;
import vip.justlive.oxygen.web.mapping.Mapping.HttpMethod;
import vip.justlive.oxygen.web.mapping.Router;
import vip.justlive.oxygen.web.mapping.StaticMapping;
import vip.justlive.oxygen.web.mapping.StaticMapping.StaticSource;
import vip.justlive.oxygen.web.view.ViewResolver;

/**
 * web插件
 *
 * @author wubo
 */
@Slf4j
public class WebPlugin implements Plugin {

  static final List<RequestParse> REQUEST_PARSES = new LinkedList<>();
  static final Map<Integer, ErrorHandler> ERROR_HANDLERS = new HashMap<>(2, 1f);
  private static final List<ParamHandler> PARAM_HANDLERS = new LinkedList<>();
  private static final StaticMapping STATIC_MAPPING = new StaticMapping();
  private static final Map<HttpMethod, Map<String, Action>> SIMPLE_ACTION_MAP = new ConcurrentHashMap<>(
      8, 1f);
  private static final Map<HttpMethod, Map<String, Action>> REGEX_ACTION_MAP = new ConcurrentHashMap<>(
      8, 1f);
  private static final Set<ViewResolver> VIEW_RESOLVERS = new HashSet<>(4);
  private static final Pattern REGEX_PATH_GROUP = Pattern.compile("\\{(\\w+)\\}");

  static {
    for (HttpMethod httpMethod : HttpMethod.values()) {
      SIMPLE_ACTION_MAP.put(httpMethod, new ConcurrentHashMap<>(4, 1f));
      REGEX_ACTION_MAP.put(httpMethod, new ConcurrentHashMap<>(4, 1f));
    }

  }

  /**
   * 增加静态资源
   *
   * @param prefix 请求前缀
   * @param basePath base路径
   */
  public static void addStaticResources(String prefix, String basePath) {
    STATIC_MAPPING.addStaticResource(prefix, basePath);
  }

  /**
   * 根据路径和请求类型获取Action
   *
   * @param path 请求路径
   * @param httpMethod 请求类型
   * @return action
   */
  public static Action findActionByPath(String path, HttpMethod httpMethod) {
    // simple
    Map<String, Action> actionMap = SIMPLE_ACTION_MAP.get(httpMethod);
    Action action = actionMap.get(path);
    if (action != null) {
      return action;
    }
    // static
    StaticSource source = STATIC_MAPPING.findStaticResource(path);
    if (source != null) {
      throw new StaticMapping.StaticException(source);
    }
    // regex
    actionMap = REGEX_ACTION_MAP.get(httpMethod);
    for (Map.Entry<String, Action> entry : actionMap.entrySet()) {
      if (Pattern.compile(entry.getKey()).matcher(path).matches()) {
        return entry.getValue();
      }
    }
    return null;
  }

  /**
   * 根据入参类型获取参数处理器
   *
   * @param dataBinder 数据绑定
   * @return ParamHandler
   */
  public static ParamHandler findParamHandler(DataBinder dataBinder) {
    for (ParamHandler paramHandler : PARAM_HANDLERS) {
      if (paramHandler.supported(dataBinder)) {
        return paramHandler;
      }
    }
    return null;
  }

  /**
   * 添加视图解析器
   *
   * @param viewResolver 视图解析器
   */
  public static void addViewResolver(ViewResolver viewResolver) {
    VIEW_RESOLVERS.add(viewResolver);
    if (log.isDebugEnabled()) {
      log.debug("add a view resolver [{}]", viewResolver.getClass());
    }
  }

  /**
   * 获取视图解析
   *
   * @param data Action执行返回
   * @return ViewResolver
   */
  public static ViewResolver findViewResolver(Object data) {
    for (ViewResolver viewResolver : VIEW_RESOLVERS) {
      if (viewResolver.supported(data)) {
        return viewResolver;
      }
    }
    return null;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 60;
  }

  @Override
  public void start() {
    loadRouter();
    loadRequestParse();
    loadParamHandler();
    loadViewResolver();
    loadStaticMapping();
    loadErrorHandler();
  }

  @Override
  public void stop() {
    SIMPLE_ACTION_MAP.clear();
    REGEX_ACTION_MAP.clear();
    REQUEST_PARSES.clear();
    ERROR_HANDLERS.clear();
  }

  private void loadRequestParse() {
    ServiceLoader<RequestParse> loader = ServiceLoader.load(RequestParse.class);
    for (RequestParse requestParse : loader) {
      REQUEST_PARSES.add(requestParse);
      if (log.isDebugEnabled()) {
        log.debug("loaded a request parser [{}]", requestParse.getClass());
      }
    }
  }

  private void loadParamHandler() {
    ServiceLoader<ParamHandler> loader = ServiceLoader.load(ParamHandler.class);
    for (ParamHandler paramHandler : loader) {
      PARAM_HANDLERS.add(paramHandler);
      if (log.isDebugEnabled()) {
        log.debug("loaded a param handler [{}]", paramHandler.getClass());
      }
    }
  }

  private void loadViewResolver() {
    ServiceLoader<ViewResolver> loader = ServiceLoader.load(ViewResolver.class);
    for (ViewResolver viewResolver : loader) {
      addViewResolver(viewResolver);
    }
  }

  private void loadRouter() {
    Set<Class<?>> routerClasses = ClassScannerPlugin.getTypesAnnotatedWith(Router.class);
    if (routerClasses == null || routerClasses.isEmpty()) {
      return;
    }
    for (Class<?> clazz : routerClasses) {
      Router router = clazz.getAnnotation(Router.class);
      if (router == null) {
        continue;
      }

      String rootPath = router.value();
      Object routerBean = IocPlugin.instanceBean(clazz);
      BeanStore.putBean(clazz.getName(), routerBean);

      parseRequest(rootPath, routerBean);
    }
  }

  private void parseRequest(String rootPath, Object routerBean) {
    Class<?> clazz = routerBean.getClass();
    Class<?> actualClass = ClassUtils.getCglibActualClass(clazz);
    try {
      for (Method method : actualClass.getDeclaredMethods()) {
        if (!method.isAnnotationPresent(Mapping.class)) {
          continue;
        }
        Method requestMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
        Mapping mapping = method.getAnnotation(Mapping.class);
        String path = mapping.value();
        HttpMethod[] httpMethods = mapping.method();
        String routePath = makePath(rootPath, path);
        HttpMethod[] realMethods = httpMethods;
        if (realMethods.length == 0) {
          realMethods = HttpMethod.values();
        }
        Action action = createAction(routePath, routerBean, requestMethod, method);
        if (log.isDebugEnabled()) {
          log.debug("mapping a router [{}] to method [{}] for request of [{}]", action.getPath(),
              method, realMethods);
        }
        for (HttpMethod httpMethod : realMethods) {
          if (action.getPathVariables().isEmpty()) {
            SIMPLE_ACTION_MAP.get(httpMethod).put(action.getPath(), action);
          } else {
            REGEX_ACTION_MAP.get(httpMethod).put(action.getPath(), action);
          }
        }
      }
    } catch (NoSuchMethodException e) {
      throw Exceptions.wrap(e);
    }
  }

  private Action createAction(String routePath, Object routerBean, Method requestMethod,
      Method method) {
    List<String> pathVariables = new LinkedList<>();
    Matcher matcher = REGEX_PATH_GROUP.matcher(routePath);
    int start = 0;
    if (matcher.find(start)) {
      do {
        pathVariables.add(matcher.group(1));
        start = matcher.end();
      } while (matcher.find(start));
      routePath = routePath.replaceAll(Constants.REGEX_PATH_VAR, Constants.REGEX_PATH_VAR_REPLACE);
    }
    return new Action(routePath, routerBean, requestMethod, method, pathVariables);
  }

  private String makePath(String parent, String child) {
    Checks.notNull(parent);
    Checks.notNull(child);
    StringBuilder sb = new StringBuilder();
    if (!parent.startsWith(Constants.ROOT_PATH)) {
      sb.append(Constants.ROOT_PATH);
    }
    sb.append(parent);
    if (parent.endsWith(Constants.ROOT_PATH)) {
      sb.deleteCharAt(sb.length() - 1);
    }
    if (!child.startsWith(Constants.ROOT_PATH)) {
      sb.append(Constants.ROOT_PATH);
    }
    sb.append(child);
    if (child.endsWith(Constants.ROOT_PATH)) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  private void loadStaticMapping() {
    WebConf conf = ConfigFactory.load(WebConf.class);
    String[] paths = conf.getDefaultStaticPaths();
    if (paths == null) {
      return;
    }
    for (String path : paths) {
      addStaticResources(conf.getDefaultStaticPrefix(), path);
    }
  }

  private void loadErrorHandler() {
    WebConf webConf = ConfigFactory.load(WebConf.class);
    try {
      ErrorHandler errorHandler;
      if (webConf.getError404Handler() != null && webConf.getError404Handler().length() > 0) {
        errorHandler = (ErrorHandler) ClassUtils
            .forName(webConf.getError404Handler(), ClassUtils.getDefaultClassLoader())
            .newInstance();
      } else {
        errorHandler = new DefaultErrorHandler(webConf.getError404Page(), Constants.NOT_FOUND);
      }
      ERROR_HANDLERS.put(Constants.NOT_FOUND, errorHandler);
      if (webConf.getError500Handler() != null && webConf.getError500Handler().length() > 0) {
        errorHandler = (ErrorHandler) ClassUtils
            .forName(webConf.getError500Handler(), ClassUtils.getDefaultClassLoader())
            .newInstance();
      } else {
        errorHandler = new DefaultErrorHandler(webConf.getError500Page(), Constants.SERVER_ERROR);
      }
      ERROR_HANDLERS.put(Constants.SERVER_ERROR, errorHandler);
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }

}
