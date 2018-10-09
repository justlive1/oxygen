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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.ioc.BeanStore;
import vip.justlive.oxygen.core.ioc.IocPlugin;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;
import vip.justlive.oxygen.core.util.Checks;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.web.handler.ParamHandler;
import vip.justlive.oxygen.web.http.RequestParse;
import vip.justlive.oxygen.web.mapping.Action;
import vip.justlive.oxygen.web.mapping.DataBinder;
import vip.justlive.oxygen.web.mapping.Request;
import vip.justlive.oxygen.web.mapping.Request.HttpMethod;
import vip.justlive.oxygen.web.mapping.Router;
import vip.justlive.oxygen.web.view.ViewResolver;

/**
 * web插件
 *
 * @author wubo
 */
public class WebPlugin implements Plugin {

  static final List<RequestParse> REQUEST_PARSES = new LinkedList<>();
  private static final List<ParamHandler> PARAM_HANDLERS = new LinkedList<>();
  private static final Map<HttpMethod, Map<String, Action>> ACTION_MAP = new ConcurrentHashMap<>(8,
      1f);
  private static final Set<ViewResolver> VIEW_RESOLVERS = new HashSet<>(4);

  static {
    for (HttpMethod httpMethod : HttpMethod.values()) {
      ACTION_MAP.put(httpMethod, new ConcurrentHashMap<>(4, 1f));
    }
  }

  /**
   * 根据路径和请求类型获取Action
   *
   * @param path 请求路径
   * @param httpMethod 请求类型
   * @return action
   */
  public static Action findActionByPath(String path, HttpMethod httpMethod) {
    Map<String, Action> actionMap = ACTION_MAP.get(httpMethod);
    Action action = actionMap.get(path);
    if (action != null) {
      return action;
    }
    // TODO path vars
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
  }


  @Override
  public void stop() {
    ACTION_MAP.clear();
    REQUEST_PARSES.clear();
  }

  private void loadRequestParse() {
    ServiceLoader<RequestParse> loader = ServiceLoader.load(RequestParse.class);
    for (RequestParse requestParse : loader) {
      REQUEST_PARSES.add(requestParse);
    }
  }

  private void loadParamHandler() {
    ServiceLoader<ParamHandler> loader = ServiceLoader.load(ParamHandler.class);
    for (ParamHandler paramHandler : loader) {
      PARAM_HANDLERS.add(paramHandler);
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
        if (!method.isAnnotationPresent(Request.class)) {
          continue;
        }
        Method requestMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
        Request request = method.getAnnotation(Request.class);
        String path = request.value();
        HttpMethod[] httpMethods = request.method();
        String routePath = makePath(rootPath, path);
        Action action = new Action(routePath, routerBean, requestMethod, method);
        HttpMethod[] realMethods = httpMethods;
        if (realMethods.length == 0) {
          realMethods = HttpMethod.values();
        }
        for (HttpMethod httpMethod : realMethods) {
          ACTION_MAP.get(httpMethod).put(action.getPath(), action);
        }
      }
    } catch (NoSuchMethodException e) {
      throw Exceptions.wrap(e);
    }
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
    // TODO path vars
    return sb.toString();
  }


}
