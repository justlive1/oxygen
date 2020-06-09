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

package vip.justlive.oxygen.core.aop.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import vip.justlive.oxygen.core.aop.AopWrapper;

/**
 * aop注解拦截
 *
 * @author wubo
 * @since 2.0.0
 */
public class AnnotationInterceptor extends AbstractInterceptor {

  private final Class<? extends Annotation> targetAnnotation;

  public AnnotationInterceptor(Class<? extends Annotation> targetAnnotation,
      AopWrapper aopWrapper) {
    super(aopWrapper);
    this.targetAnnotation = targetAnnotation;
  }

  @Override
  public boolean match(Method method) {
    for (Annotation annotation : method.getAnnotations()) {
      if (annotation.annotationType() == this.targetAnnotation) {
        return true;
      }
    }
    return false;
  }
}
