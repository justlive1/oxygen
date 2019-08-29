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

import java.util.function.Function;
import lombok.Data;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * 数据绑定
 *
 * @author wubo
 */
@Data
public class DataBinder {

  /**
   * 数据绑定名称
   */
  private String name;
  /**
   * 数据类型
   */
  private Class<?> type;
  /**
   * 默认值
   */
  private String defaultValue;

  private Function<RoutingContext, Object> func;

}
