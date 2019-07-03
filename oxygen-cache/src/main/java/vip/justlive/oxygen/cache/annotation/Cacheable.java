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
package vip.justlive.oxygen.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 自动实现cache注解
 * <br>
 * 仅支持加在方法上
 *
 * @author wubo
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {

  /**
   * 缓存名称
   *
   * @return name
   */
  String value() default "";

  /**
   * 缓存key
   * <br>
   * args[0], args[1][name]
   *
   * @return key
   */
  String key() default "";

  /**
   * 缓存时间
   *
   * @return duration
   */
  long duration() default -1;

  /**
   * 缓存时间单位（默认：秒）
   *
   * @return unit
   */
  TimeUnit timeUnit() default TimeUnit.SECONDS;

}
