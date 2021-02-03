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
package vip.justlive.oxygen.web.router;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import vip.justlive.oxygen.core.aop.invoke.Invoker;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.web.Context;
import vip.justlive.oxygen.web.bind.DataBinder;
import vip.justlive.oxygen.web.result.Result;

/**
 * 注解路由处理
 *
 * @author wubo
 */
public class AnnotationRouteHandler implements RouteHandler {

  private final Object router;
  private final Method method;
  private final DataBinder[] dataBinders;
  private final Invoker invoker;

  public AnnotationRouteHandler(Object router, Method proxyMethod, Method method) {
    this.router = router;
    this.method = method;
    this.dataBinders = new DataBinder[method.getParameterCount()];
    this.invoker = ClassUtils.generateInvoker(this.router, proxyMethod);
    parse();
  }

  private void parse() {
    Parameter[] parameters = method.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      dataBinders[i] = Context.bind(parameters[i]);
    }
  }

  @Override
  public void handle(RoutingContext ctx) {
    Object[] args = new Object[dataBinders.length];
    for (int i = 0; i < dataBinders.length; i++) {
      args[i] = dataBinders[i].getFunc().apply(ctx);
    }
    Object result = invoker.invoke(args);
    if (Result.class.isAssignableFrom(method.getReturnType())) {
      ctx.response().setResult((Result) result);
    } else if (result != null) {
      ctx.response().setResult(Result.json(result));
    }
  }

  @Override
  public String toString() {
    return String
        .format("@Router->%s#%s", ClassUtils.getActualClass(router.getClass()), method.getName());
  }
}
