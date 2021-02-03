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

import java.lang.reflect.Method;
import vip.justlive.oxygen.core.aop.Aspect.TYPE;
import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.bean.Inject;

/**
 * @author wubo
 */
@Bean
public class Conf extends BaseConf {


  @Inject
  public Conf(LogService logService) {
    super(logService);
  }

  public static void main(String[] args) {
    for (Method method : Conf.class.getMethods()) {
      System.out.println(method.getName() + method.getAnnotation(Aspect.class));
    }
  }

  @Aspect(annotation = Log.class, type = {TYPE.BEFORE, TYPE.AFTER, TYPE.CATCHING}, order = 1)
  public void log() {
    System.out.println("aop all log");
  }

  @Aspect(method = "vip.justlive.**.print", type = TYPE.AFTER)
  public void log2(Invocation invocation) {
    System.out.println("aop after log2 " + invocation.getMethod());
    logService.log();
  }

  @Aspect(annotation = Log.class, type = TYPE.CATCHING)
  public void log3(Invocation invocation) {
    System.out.println("aop catching log3 " + invocation.getMethod());
    logService.log();
  }
}
