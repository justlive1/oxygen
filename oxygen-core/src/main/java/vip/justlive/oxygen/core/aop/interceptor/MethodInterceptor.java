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

package vip.justlive.oxygen.core.aop.interceptor;

import java.lang.reflect.Method;
import vip.justlive.oxygen.core.aop.AopWrapper;
import vip.justlive.oxygen.core.util.base.SplitterMatcher;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * method方法表达式aop拦截
 *
 * @author wubo
 */
public class MethodInterceptor extends AbstractInterceptor {

  private static final SplitterMatcher MATCHER = new SplitterMatcher('.');
  private final String expression;

  public MethodInterceptor(String expression, AopWrapper aopWrapper) {
    super(aopWrapper);
    this.expression = expression;
  }

  @Override
  public boolean match(Method method) {
    String path = method.getDeclaringClass().getName().concat(Strings.DOT).concat(method.getName());
    return MATCHER.match(expression, path);
  }
}
