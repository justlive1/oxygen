/*
 * Copyright (C) 2020 the original author or authors.
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
import java.util.Arrays;
import java.util.Collections;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.bean.Singleton;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.Urls;
import vip.justlive.oxygen.core.util.net.http.HttpMethod;
import vip.justlive.oxygen.core.util.scan.ClassScannerPlugin;
import vip.justlive.oxygen.web.annotation.Mapping;
import vip.justlive.oxygen.web.annotation.Router;
import vip.justlive.oxygen.web.bind.ParamBinder;
import vip.justlive.oxygen.web.hook.WebHook;
import vip.justlive.oxygen.web.http.Parser;
import vip.justlive.oxygen.web.http.SessionStore;
import vip.justlive.oxygen.web.result.ResultHandler;
import vip.justlive.oxygen.web.router.AnnotationRouteHandler;
import vip.justlive.oxygen.web.router.Route;

/**
 * web插件
 *
 * @author wubo
 */
public class WebPlugin implements Plugin {

  @Override
  public int order() {
    return Integer.MIN_VALUE + 900;
  }

  @Override
  public void start() {
    loadWebEnv();
    loadStaticRoute();
    loadAnnotationRouter();
    vip.justlive.oxygen.web.router.Router.build();
  }

  @Override
  public void stop() {
    Context.PARSERS.clear();
    Context.BINDERS.clear();
    Context.HANDLERS.clear();
    Context.HOOKS.clear();
    vip.justlive.oxygen.web.router.Router.clear();
  }

  private void loadStaticRoute() {
    String[] paths = WebConfigKeys.STATIC_PATH.castValue(String[].class);
    if (paths == null) {
      return;
    }
    vip.justlive.oxygen.web.router.Router.staticRoute()
        .prefix(WebConfigKeys.STATIC_PREFIX.getValue())
        .locations(Arrays.asList(paths))
        .cachingEnabled(WebConfigKeys.VIEW_CACHE.castValue(boolean.class))
        .maxAge(WebConfigKeys.STATIC_CACHE.castValue(int.class));
  }

  private void loadAnnotationRouter() {
    ClassScannerPlugin.getTypesAnnotatedWith(Router.class).forEach(clazz -> {
      Router router = clazz.getAnnotation(Router.class);
      if (router == null) {
        return;
      }
      parseRouter(router.value(), Singleton.get(clazz.getName()));
    });
  }

  private void parseRouter(String rootPath, Object routerBean) {
    Class<?> clazz = routerBean.getClass();
    Class<?> actualClass = ClassUtils.getActualClass(clazz);
    try {
      for (Method method : actualClass.getDeclaredMethods()) {
        if (!method.isAnnotationPresent(Mapping.class)) {
          continue;
        }
        Method requestMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
        Mapping mapping = method.getAnnotation(Mapping.class);
        String routePath = Urls.concat(rootPath, mapping.value());
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

  private void loadWebEnv() {
    SessionStore sessionStore = Singleton.get(SessionStore.class);
    if (sessionStore != null) {
      Context.SESSION_MANAGER.setStore(sessionStore);
    }
    Context.SESSION_MANAGER.setExpired(WebConfigKeys.SESSION_EXPIRED.castValue(long.class));

    Context.PARSERS.addAll(Singleton.getCastMap(Parser.class).values());
    Collections.sort(Context.PARSERS);

    Context.BINDERS.addAll(Singleton.getCastMap(ParamBinder.class).values());
    Collections.sort(Context.BINDERS);

    Context.HANDLERS.addAll(Singleton.getCastMap(ResultHandler.class).values());
    Collections.sort(Context.HANDLERS);

    Context.HOOKS.addAll(Singleton.getCastMap(WebHook.class).values());
    Collections.sort(Context.HOOKS);
  }

}
