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
package vip.justlive.oxygen.core.job;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定时任务注解
 *
 * @author wubo
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scheduled {

  /**
   * cron
   *
   * @return cron
   */
  String cron() default "";

  /**
   * 固定延迟 结束时间-开始时间
   * <br>
   * 单位毫秒
   *
   * @return 延迟
   */
  String fixedDelay() default "";

  /**
   * 固定周期 开始时间-开始时间
   * <br>
   * 单位毫秒
   *
   * @return 周期
   */
  String fixedRate() default "";

  /**
   * 第一次启动延迟
   * <br>
   * 单位毫秒
   *
   * @return 延迟
   */
  String initialDelay() default "";

}
