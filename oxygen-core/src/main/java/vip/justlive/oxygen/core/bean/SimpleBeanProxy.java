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

import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;

/**
 * simple bean proxy
 *
 * @author wubo
 * @since 2.0.0
 */
public class SimpleBeanProxy implements BeanProxy {

  @Override
  public <T> T proxy(Class<T> clazz, Object... args) {
    try {
      return clazz.getConstructor(ClassUtils.getConstructorArgsTypes(clazz, args))
          .newInstance(args);
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }

}
