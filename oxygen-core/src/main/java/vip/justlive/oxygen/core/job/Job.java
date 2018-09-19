/*
 * Copyright (C) 2018 justlive1
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

/**
 * job类
 *
 * @author wubo
 */
@Slf4j
public class Job implements Runnable {

  private final Object target;
  private final Method method;

  public Job(Object target, Method method) {
    this.target = target;
    this.method = method;
  }

  @Override
  public void run() {
    try {
      method.invoke(target);
    } catch (IllegalAccessException | InvocationTargetException e) {
      log.error("execute job error", e);
    }
  }
}
