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
package vip.justlive.oxygen.core.aop.invoke;

/**
 * 执行器
 *
 * @author wubo
 */
public interface Invoker {

  /**
   * 执行方法，无参
   *
   * @return 结果
   */
  Object invoke();

  /**
   * 执行方法
   *
   * @param args 参数
   * @return 结果
   */
  Object invoke(Object[] args);
}
