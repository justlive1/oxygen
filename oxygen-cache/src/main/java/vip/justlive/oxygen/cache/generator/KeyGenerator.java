/*
 * Copyright (C) 2019 the original author or authors.
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
package vip.justlive.oxygen.cache.generator;

import java.lang.reflect.Method;

/**
 * 缓存key生成器
 *
 * @author wubo
 */
public interface KeyGenerator {

  /**
   * 通过调用方法和入参生成key
   *
   * @param target the target instance
   * @param method the method being called
   * @param params the method parameters (with any var-args expanded)
   * @return a generated key
   */
  Object generate(Object target, Method method, Object... params);
}
