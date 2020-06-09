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
package vip.justlive.oxygen.web.bind;

import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Parameter;
import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.convert.DefaultConverterService;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.web.annotation.Param;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * Param注解参数绑定
 *
 * @author wubo
 */
@Bean
public class BasicParamBinder implements ParamBinder {

  @Override
  public boolean supported(Parameter parameter) {
    return parameter.isAnnotationPresent(Param.class);
  }

  @Override
  public DataBinder build(Parameter parameter) {
    DataBinder dataBinder = new DataBinder();
    dataBinder.setType(parameter.getType());
    Param param = parameter.getAnnotation(Param.class);
    dataBinder.setName(Strings.firstNonNull(param.value(), parameter.getName()));
    if (param.defaultValue().length() > 0) {
      dataBinder.setDefaultValue(param.defaultValue());
    }
    dataBinder.setFunc(ctx -> this.func(ctx, dataBinder));
    return dataBinder;
  }

  private Object func(RoutingContext ctx, DataBinder dataBinder) {
    DefaultConverterService converterService = DefaultConverterService.sharedConverterService();
    Object value = ctx.request().getParam(dataBinder.getName());
    if (value == null) {
      value = ctx.request().getBodyParams().get(dataBinder.getName());
    }
    if (value == null) {
      value = dataBinder.getDefaultValue();
    }
    if (value != null && converterService.canConverter(value.getClass(), dataBinder.getType())) {
      return converterService.convert(value, dataBinder.getType());
    }
    if (value instanceof JSONObject) {
      return ((JSONObject) value).toJavaObject(dataBinder.getType());
    }
    return ctx.bindParam(dataBinder.getType());
  }
}
