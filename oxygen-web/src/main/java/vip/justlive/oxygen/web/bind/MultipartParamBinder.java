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
import vip.justlive.oxygen.web.annotation.MultipartParam;
import vip.justlive.oxygen.web.http.MultipartItem;

/**
 * 上传文件参数绑定
 *
 * @author wubo
 */
public class MultipartParamBinder implements ParamBinder {

  @Override
  public boolean supported(Parameter parameter) {
    return MultipartItem.class == parameter.getType();
  }

  @Override
  public DataBinder build(Parameter parameter) {
    DataBinder dataBinder = new DataBinder();
    dataBinder.setType(parameter.getType());
    dataBinder.setName(parameter.getName());
    MultipartParam param = parameter.getAnnotation(MultipartParam.class);
    if (param != null && param.value().length() > 0) {
      dataBinder.setName(param.value());
    }
    dataBinder.setFunc(ctx -> ctx.request().getMultipartItem(dataBinder.getName()));
    return dataBinder;
  }
}
