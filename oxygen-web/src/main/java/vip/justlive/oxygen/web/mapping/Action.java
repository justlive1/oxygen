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
package vip.justlive.oxygen.web.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import vip.justlive.oxygen.web.WebPlugin;
import vip.justlive.oxygen.web.handler.ParamHandler;
import vip.justlive.oxygen.web.mapping.DataBinder.SCOPE;

/**
 * action
 *
 * @author wubo
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Action {

  private final String path;
  private final Object router;
  private final Method method;
  private final DataBinder[] dataBinders;

  public Action(String path, Object router, Method method, Method actualMethod) {
    this.path = path;
    this.router = router;
    this.method = method;
    this.dataBinders = new DataBinder[actualMethod.getParameterCount()];
    this.init(actualMethod);
  }

  /**
   * 是否需要渲染视图
   *
   * @return true需要渲染
   */
  public boolean needRenderView() {
    return method.getReturnType() != Void.TYPE;
  }

  /**
   * 执行action
   *
   * @return result
   * @throws InvocationTargetException 反射出错
   * @throws IllegalAccessException 反射出错
   */
  public Object invoke() throws InvocationTargetException, IllegalAccessException {
    Object[] args = new Object[dataBinders.length];
    if (args.length > 0) {
      for (int i = 0; i < args.length; i++) {
        DataBinder dataBinder = dataBinders[i];
        ParamHandler paramHandler = WebPlugin.findParamHandler(dataBinder);
        if (paramHandler != null) {
          Object result = paramHandler.handle(dataBinder);
          args[i] = result;
        }
      }
    }
    return method.invoke(router, args);
  }

  void init(Method method) {
    Parameter[] parameters = method.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      DataBinder dataBinder = new DataBinder();
      dataBinder.setName(parameters[i].getName());
      dataBinder.setType(parameters[i].getType());
      parseAnnotation(dataBinder, parameters[i]);
      dataBinders[i] = dataBinder;
    }
  }

  private void parseAnnotation(DataBinder dataBinder, Parameter parameter) {
    Param param = parameter.getAnnotation(Param.class);
    if (param != null) {
      fillDataBinder(dataBinder, param.value(), param.defaultValue());
      return;
    }
    HeaderParam headerParam = parameter.getAnnotation(HeaderParam.class);
    if (headerParam != null) {
      fillDataBinder(dataBinder, headerParam.value(), headerParam.defaultValue());
      dataBinder.setScope(SCOPE.HEADER);
      return;
    }
    CookieParam cookieParam = parameter.getAnnotation(CookieParam.class);
    if (cookieParam != null) {
      fillDataBinder(dataBinder, cookieParam.value(), cookieParam.defaultValue());
      dataBinder.setScope(SCOPE.COOKIE);
    }
  }

  private void fillDataBinder(DataBinder dataBinder, String name, String defaultValue) {
    if (name.length() > 0) {
      dataBinder.setName(name);
    }
    if (defaultValue.length() > 0) {
      dataBinder.setDefaultValue(defaultValue);
    }
  }
}
