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
package vip.justlive.oxygen.web.bind;

import java.lang.reflect.Parameter;
import vip.justlive.oxygen.core.convert.DefaultConverterService;
import vip.justlive.oxygen.core.util.MoreObjects;
import vip.justlive.oxygen.web.annotation.Param;

/**
 * Param注解参数绑定
 *
 * @author wubo
 */
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
    dataBinder.setName(MoreObjects.firstNonEmpty(param.value(), parameter.getName()));
    if (param.defaultValue().length() > 0) {
      dataBinder.setDefaultValue(param.defaultValue());
    }
    dataBinder.setFunc(ctx -> {
      DefaultConverterService converterService = DefaultConverterService.sharedConverterService();
      if (converterService.canConverter(String.class, dataBinder.getType())) {
        return converterService.convert(MoreObjects
            .firstOrNull(ctx.request().getParam(dataBinder.getName()),
                dataBinder.getDefaultValue()), dataBinder.getType());
      }
      return ctx.bindParam(dataBinder.getType());
    });
    return dataBinder;
  }

}
