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
package vip.justlive.oxygen.core.bean;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import vip.justlive.oxygen.core.Order;
import vip.justlive.oxygen.core.util.base.ClassUtils;

/**
 * bean info
 *
 * @author wubo
 * @since 2.0.0
 */
@Getter
@ToString
@EqualsAndHashCode
public class BeanInfo implements Order {

  private final String name;
  private final Object bean;
  private final int order;
  private final Class<?> type;

  public BeanInfo(String name, Object bean, int order) {
    this.name = name;
    this.bean = bean;
    this.order = order;
    this.type = ClassUtils.getActualClass(bean.getClass());
  }

  @Override
  public int order() {
    return order;
  }
}
