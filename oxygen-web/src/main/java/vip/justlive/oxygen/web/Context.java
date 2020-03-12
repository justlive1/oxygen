/*
 * Copyright (C) 2019 the original author or authors.
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

import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.web.bind.DataBinder;
import vip.justlive.oxygen.web.bind.ParamBinder;
import vip.justlive.oxygen.web.hook.WebHook;
import vip.justlive.oxygen.web.http.Parser;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.http.Session;
import vip.justlive.oxygen.web.http.SessionManager;
import vip.justlive.oxygen.web.result.ResultHandler;
import vip.justlive.oxygen.web.router.RouteHandler;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * context
 *
 * @author wubo
 */
@UtilityClass
public class Context {

  public static final SessionManager SESSION_MANAGER = new SessionManager();
  static final List<ResultHandler> HANDLERS = new LinkedList<>();
  static final List<ParamBinder> BINDERS = new LinkedList<>();
  static final List<WebHook> HOOKS = new LinkedList<>();
  static final List<Parser> PARSERS = new LinkedList<>();


  /**
   * 绑定参数
   *
   * @param parameter 参数
   * @return 数据绑定
   */
  public static DataBinder bind(Parameter parameter) {
    for (ParamBinder binder : Context.BINDERS) {
      if (binder.supported(parameter)) {
        return binder.build(parameter);
      }
    }
    return null;
  }

  /**
   * dispatch
   *
   * @param ctx 上下文
   */
  public static void dispatch(RoutingContext ctx) {
    try {
      Context.parseRequest(ctx.request());
      RouteHandler handler = ctx.request().getRouteHandler();
      if (handler == null) {
        RouteHandler.notFound(ctx);
        return;
      }
      if (!Context.invokeBefore(ctx)) {
        return;
      }
      handler.handle(ctx);
      Context.invokeAfter(ctx);
    } catch (Exception e) {
      RouteHandler.exception(ctx, e);
    } finally {
      Context.invokeFinished(ctx);
      Context.restoreSession(ctx.request(), ctx.response());
      Context.handleResult(ctx);
    }
  }

  /**
   * finally中执行
   *
   * @param ctx 上下文
   */
  private static void invokeFinished(RoutingContext ctx) {
    for (int i = HOOKS.size() - 1; i >= 0; i--) {
      HOOKS.get(i).finished(ctx);
    }
  }

  /**
   * 执行后置处理
   *
   * @param ctx 上下文
   */
  private static void invokeAfter(RoutingContext ctx) {
    for (int i = HOOKS.size() - 1; i >= 0; i--) {
      HOOKS.get(i).after(ctx);
    }
  }

  /**
   * 执行前置处理
   *
   * @param ctx 上下文
   * @return 返回false中断处理
   */
  private static boolean invokeBefore(RoutingContext ctx) {
    for (WebHook webHook : HOOKS) {
      if (!webHook.before(ctx)) {
        return false;
      }
    }
    return true;
  }

  /**
   * 解析请求
   *
   * @param request 请求
   */
  private static void parseRequest(Request request) {
    PARSERS.forEach(parser -> parser.parse(request));
  }

  /**
   * 重新保存session
   *
   * @param request 请求
   * @param response 响应
   */
  private static void restoreSession(Request request, Response response) {
    if (request.getSession() != null && !request.getSession().getId()
        .equals(request.getCookieValue(Session.SESSION_COOKIE_KEY))) {
      response.setCookie(Session.SESSION_COOKIE_KEY, request.getSession().getId());
    }
    SESSION_MANAGER.restoreSession(request.getSession());
  }

  /**
   * 处理结果
   *
   * @param ctx 上下文
   */
  private static void handleResult(RoutingContext ctx) {
    for (ResultHandler resultHandler : Context.HANDLERS) {
      if (resultHandler.support(ctx.response().getResult())) {
        resultHandler.apply(ctx, ctx.response().getResult());
        break;
      }
    }
  }

}
