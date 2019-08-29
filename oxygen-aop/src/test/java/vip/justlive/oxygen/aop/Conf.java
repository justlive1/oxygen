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
package vip.justlive.oxygen.aop;

import vip.justlive.oxygen.aop.annotation.Aspect;
import vip.justlive.oxygen.aop.annotation.Aspect.TYPE;
import vip.justlive.oxygen.ioc.annotation.Bean;
import vip.justlive.oxygen.ioc.annotation.Inject;

/**
 * @author wubo
 */
@Bean
public class Conf {

  private final LogService logService;

  @Inject
  public Conf(LogService logService) {
    this.logService = logService;
  }

  @Aspect(annotation = Log.class, type = {TYPE.BEFORE, TYPE.AFTER, TYPE.CATCHING}, order = 1)
  public void log() {
    System.out.println("aop all log");
  }

  @Aspect(annotation = Log.class, type = TYPE.BEFORE)
  public boolean log1(Invocation invocation) {
    System.out.println("aop before log1 " + invocation.getMethod());
    logService.log();
    return true;
  }

  @Aspect(annotation = Log.class, type = TYPE.AFTER)
  public void log2(Invocation invocation) {
    System.out.println("aop after log2 " + invocation.getMethod());
    logService.log();
  }

  @Aspect(annotation = Log.class, type = TYPE.CATCHING)
  public void log3(Invocation invocation) {
    System.out.println("aop catching log3 " + invocation.getMethod());
    logService.log();
  }

  @Log
  public void print() {
    System.out.println("print");
  }
}
