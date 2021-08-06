/*
 * Copyright (C) 2021 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;
import vip.justlive.oxygen.core.aop.invoke.Invoker;

/**
 * 注解job实现类
 *
 * @author wubo
 */
public class AnnotationJob implements Job {

  private static final Map<String, Invoker> INVOKERS = new HashMap<>(4);

  static void put(String key, Invoker invoker) {
    INVOKERS.put(key, invoker);
  }

  @Override
  public void execute(JobContext ctx) {
    String jobKey = ctx.getJobInfo().getKey();
    Invoker invoker = INVOKERS.get(jobKey);
    if (invoker == null) {
      throw new IllegalArgumentException("invoker not found");
    }
    invoker.invoke();
  }
}
