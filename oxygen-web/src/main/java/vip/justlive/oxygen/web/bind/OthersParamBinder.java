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
package vip.justlive.oxygen.web.bind;

import java.lang.reflect.Parameter;
import vip.justlive.oxygen.ioc.annotation.Bean;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * 其他参数绑定，优先级最低
 *
 * @author wubo
 */
@Bean
public class OthersParamBinder implements ParamBinder {

  @Override
  public int order() {
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean supported(Parameter parameter) {
    return true;
  }

  @Override
  public DataBinder build(Parameter parameter) {
    DataBinder dataBinder = new DataBinder();
    dataBinder.setType(parameter.getType());
    dataBinder.setName(parameter.getName());
    dataBinder.setFunc(ctx -> {
      if (RoutingContext.class.isAssignableFrom(parameter.getType())) {
        return ctx;
      }
      return null;
    });
    return dataBinder;
  }

}
