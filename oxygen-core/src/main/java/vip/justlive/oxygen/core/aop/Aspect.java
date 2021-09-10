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
package vip.justlive.oxygen.core.aop;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * aspect
 *
 * @author wubo
 * @since 2.0.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {

  /**
   * 切面类型
   *
   * @return types
   */
  TYPE[] type() default {};

  /**
   * 增强的注解
   *
   * @return annotation
   */
  Class<? extends Annotation> annotation() default Annotation.class;

  /**
   * 方法名表达式
   *
   * @return expression
   *
   * <pre>
   * &#064;Aspect(method = "vip.justlive.**.insert*")
   * </pre>
   *
   * <br>
   */
  String method() default "";

  /**
   * 优先级，越小优先级越大
   *
   * @return order
   */
  int order() default Integer.MAX_VALUE;

  enum TYPE {
    /**
     * 前置
     */
    BEFORE,
    /**
     * 后置
     */
    AFTER,
    /**
     * 异常
     */
    CATCHING
  }

}
