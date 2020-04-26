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

package vip.justlive.oxygen.jdbc.handler;

/**
 * 属性转换处理
 *
 * @author wubo
 */
public interface PropertyHandler {

  /**
   * 是否支持该类型处理
   *
   * @param type type
   * @param value value
   * @return true is supported
   */
  boolean supported(Class<?> type, Object value);

  /**
   * 类型转换
   *
   * @param type type
   * @param value value
   * @return 数据
   */
  Object cast(Class<?> type, Object value);
}
