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
package vip.justlive.oxygen.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import vip.justlive.oxygen.core.util.net.http.HttpMethod;

/**
 * 请求映射
 *
 * @author wubo
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {

  /**
   * 请求路径 (e.g. "/myPath.do").
   *
   * @return path of request
   */
  String value();

  /**
   * 请求类型:GET, POST, HEAD, OPTIONS, PUT, PATCH, DELETE, TRACE.
   *
   * @return 支持的类型 默认{}为全部支持
   */
  HttpMethod[] method() default {};

}
