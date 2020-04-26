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

package vip.justlive.oxygen.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.ioc.annotation.PostConstruct;
import vip.justlive.oxygen.ioc.annotation.PreDestroy;

/**
 * bean生命周期插件
 *
 * @author wubo
 */
public class BeanLifeCyclePlugin implements Plugin {

  @Override
  public int order() {
    return Integer.MIN_VALUE + 1000;
  }

  @Override
  public void start() {
    methodInvoke(PostConstruct.class);
  }

  @Override
  public void stop() {
    methodInvoke(PreDestroy.class);
  }

  private void methodInvoke(Class<? extends Annotation> annotation) {
    for (Object bean : IocPlugin.beanStore().getBeans()) {
      for (Method method : ClassUtils
          .getMethodsAnnotatedWith(bean.getClass(), annotation)) {
        ClassUtils.methodInvoke(method, bean);
      }
    }
  }
}
