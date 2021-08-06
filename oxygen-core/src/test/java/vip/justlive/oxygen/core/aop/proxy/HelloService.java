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
package vip.justlive.oxygen.core.aop.proxy;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.aop.LogService;

/**
 * @author wubo
 */
@RequiredArgsConstructor
public class HelloService<T extends Map<String, Integer>> {

  private final LogService service;

  public String fun0(int a, String b, T obj) {
    System.out.println("real fun0");
    return b;
  }

  public void fun1() throws IllegalStateException, RuntimeException {
    System.out.println("real fun1");
  }

  public void fun2(Map<String, Integer> map) {
    System.out.println("real fun2");
  }

  public void fun3(Integer a, String... strings) {
    System.out.println("real fun3");
  }

  public void fun4(Integer a, String[]... strings) {
    System.out.println("real fun4");
  }

  public <R> void fun5(Class<R> type) {
    System.out.println("real fun5");
    service.log();
  }
}
