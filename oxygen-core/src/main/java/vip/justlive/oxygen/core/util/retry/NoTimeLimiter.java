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
package vip.justlive.oxygen.core.util.retry;

import java.util.concurrent.Callable;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 无超时限制
 *
 * @author wubo
 */
public class NoTimeLimiter<V> implements TimeLimiter<V> {

  @Override
  public V call(Callable<V> callable) {
    try {
      return callable.call();
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }
}
