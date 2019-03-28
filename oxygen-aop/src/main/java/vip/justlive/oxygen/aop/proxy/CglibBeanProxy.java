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
package vip.justlive.oxygen.aop.proxy;

import vip.justlive.oxygen.ioc.store.BeanProxy;

/**
 * cglib bean proxy
 *
 * @author wubo
 * @since 2.0.0
 */
public class CglibBeanProxy implements BeanProxy {

  @Override
  public Object proxy(Class<?> clazz, Object... args) {
    return CglibProxy.proxy(clazz, args);
  }

  @Override
  public int order() {
    return -100;
  }
}
