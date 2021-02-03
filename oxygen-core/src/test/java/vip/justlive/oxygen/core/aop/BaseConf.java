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

import vip.justlive.oxygen.core.aop.Aspect.TYPE;

/**
 * @author wubo
 */
public class BaseConf {

  final LogService logService;

  public BaseConf(LogService logService) {
    this.logService = logService;
  }

  @Log
  public void print() {
    System.out.println("print");
  }

  @Aspect(annotation = Log.class, type = TYPE.BEFORE)
  public boolean log1(Invocation invocation) {
    System.out.println("aop before log1 " + invocation.getMethod());
    logService.log();
    return true;
  }

}
