/*
 *  Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */
package vip.justlive.oxygen.web;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.ResourceUtils;
import vip.justlive.oxygen.ioc.IocPlugin;
import vip.justlive.oxygen.web.annotation.Mapping;
import vip.justlive.oxygen.web.annotation.Router;
import vip.justlive.oxygen.web.exception.ExceptionHandler;
import vip.justlive.oxygen.web.exception.ExceptionHandlerImpl;
import vip.justlive.oxygen.web.http.HttpMethod;
import vip.justlive.oxygen.web.http.SessionManager;
import vip.justlive.oxygen.web.http.SessionStore;
import vip.justlive.oxygen.web.router.AnnotationRouteHandler;
import vip.justlive.oxygen.web.router.Route;
import vip.justlive.oxygen.web.servlet.DispatcherServlet;

/**
 * web插件
 *
 * @author wubo
 */
@Slf4j
public class WebPlugin implements Plugin {

  public static final SessionManager SESSION_MANAGER = new SessionManager();

  @Override
  public int order() {
    return Integer.MIN_VALUE + 60;
  }

  @Override
  public void start() {
    loadStaticRoute();
    loadAnnotationRouter();
    loadErrorHandler();
    loadSessionManager();
    DispatcherServlet.load();
    vip.justlive.oxygen.web.router.Router.build();
  }

  @Override
  public void stop() {
    DispatcherServlet.clear();
    vip.justlive.oxygen.web.router.Router.clear();
  }

  private void loadStaticRoute() {
    WebConf conf = ConfigFactory.load(WebConf.class);
    String[] paths = conf.getStaticPaths();
    if (paths == null) {
      return;
    }
    vip.justlive.oxygen.web.router.Router.staticRoute().prefix(conf.getStaticPrefix())
        .locations(Arrays.asList(paths)).cachingEnabled(conf.isViewCacheEnabled())
        .maxAge(conf.getStaticCache());
  }

  private void loadAnnotationRouter() {
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
      if (!IocPlugin.tryInstance(clazz, clazz.getName(), Integer.MAX_VALUE)) {
        throw Exceptions.fail(String.format("实例化Router失败 %s", clazz));
      }
      parseRouter(rootPath, IocPlugin.beanStore().getBean(clazz.getName()));
    }
  }

  private void parseRouter(String rootPath, Object routerBean) {
    Class<?> clazz = routerBean.getClass();
    Class<?> actualClass = ClassUtils.getCglibActualClass(clazz);
    try {
      for (Method method : actualClass.getDeclaredMethods()) {
        if (!method.isAnnotationPresent(Mapping.class)) {
          continue;
        }
        Method requestMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
        Mapping mapping = method.getAnnotation(Mapping.class);
        String routePath = ResourceUtils.concat(rootPath, mapping.value());
        Route route = vip.justlive.oxygen.web.router.Router.router().path(routePath);
        for (HttpMethod httpMethod : mapping.method()) {
          route.method(httpMethod);
        }
        route.handler(new AnnotationRouteHandler(routerBean, requestMethod, method));
      }
    } catch (NoSuchMethodException e) {
      throw Exceptions.wrap(e);
    }
  }

  private void loadErrorHandler() {
    ExceptionHandler handler = IocPlugin.beanStore().getBean(ExceptionHandler.class);
    if (handler != null) {
      if (log.isDebugEnabled()) {
        log.debug("loaded error handler of user {}", handler);
      }
    } else {
      handler = new ExceptionHandlerImpl();
      IocPlugin.beanStore().addBean(handler);
      if (log.isDebugEnabled()) {
        log.debug("loaded default error handler {}", handler);
      }
    }
  }

  private void loadSessionManager() {
    SessionStore sessionStore = IocPlugin.beanStore().getBean(SessionStore.class);
    if (sessionStore != null) {
      SESSION_MANAGER.setStore(sessionStore);
    }
    SESSION_MANAGER.setExpired(ConfigFactory.load(WebConf.class).getSessionExpired());
  }
}
