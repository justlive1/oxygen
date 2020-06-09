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

import java.lang.reflect.Parameter;
import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.convert.DefaultConverterService;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.web.annotation.CookieParam;

/**
 * cookie 参数绑定
 *
 * @author wubo
 */
@Bean
public class CookieParamBinder implements ParamBinder {

  @Override
  public boolean supported(Parameter parameter) {
    return parameter.isAnnotationPresent(CookieParam.class);
  }

  @Override
  public DataBinder build(Parameter parameter) {
    DataBinder dataBinder = new DataBinder();
    dataBinder.setType(parameter.getType());
    CookieParam param = parameter.getAnnotation(CookieParam.class);
    dataBinder.setName(Strings.firstNonNull(param.value(), parameter.getName()));
    if (param.defaultValue().length() > 0) {
      dataBinder.setDefaultValue(param.defaultValue());
    }
    dataBinder.setFunc(ctx -> {
      DefaultConverterService converterService = DefaultConverterService.sharedConverterService();
      if (converterService.canConverter(String.class, dataBinder.getType())) {
        return converterService.convert(MoreObjects
            .firstOrNull(ctx.request().getCookieValue(dataBinder.getName()),
                dataBinder.getDefaultValue()), dataBinder.getType());
      }
      return ctx.bindCookie(dataBinder.getType());
    });
    return dataBinder;
  }
}
