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
package vip.justlive.oxygen.core.aop;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * aop拦截
 *
 * @author wubo
 */
public abstract class AopInterceptor implements Interceptor {

  private final List<AopWrapper> aopWrapperList;

  public AopInterceptor(List<AopWrapper> aopWrapperList) {
    this.aopWrapperList = aopWrapperList;
  }

  protected boolean doIntercept(Invocation invocation) {
    try {
      for (AopWrapper aopWrapper : aopWrapperList) {
        int count = aopWrapper.getMethod().getParameterCount();
        check(aopWrapper);
        if (count == 0) {
          aopWrapper.getMethod().invoke(aopWrapper.getTarget());
        } else if (count == 1) {
          Class<?> returnType = aopWrapper.getMethod().getReturnType();
          if (returnType == Void.TYPE) {
            aopWrapper.getMethod().invoke(aopWrapper.getTarget(), invocation);
          } else if (returnType == boolean.class) {
            boolean b = (boolean) aopWrapper.getMethod().invoke(aopWrapper.getTarget(), invocation);
            if (!b) {
              return false;
            }
          }
        }
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw Exceptions.wrap(e);
    }
    return true;
  }

  private void check(AopWrapper aopWrapper) {
    int count = aopWrapper.getMethod().getParameterCount();
    boolean b = (count == 1 && aopWrapper.getMethod().getParameterTypes()[0] != Invocation.class)
        || count > 1;
    if (b) {
      throw new IllegalArgumentException("aop方法入参只能为[Invocation]类型");
    }
    Class<?> returnType = aopWrapper.getMethod().getReturnType();
    if (returnType != Void.TYPE && returnType != boolean.class) {
      throw new IllegalArgumentException("aop方法返回值只能为void或boolean");
    }
  }
}

/**
 * 前置拦截
 *
 * @author wubo
 */
class BeforeInterceptor extends AopInterceptor {

  BeforeInterceptor(List<AopWrapper> aopWrapperList) {
    super(aopWrapperList);
  }

  @Override
  public boolean before(Invocation invocation) {
    return doIntercept(invocation);
  }
}

/**
 * 后置拦截
 *
 * @author wubo
 */
class AfterInterceptor extends AopInterceptor {

  AfterInterceptor(List<AopWrapper> aopWrapperList) {
    super(aopWrapperList);
  }

  @Override
  public boolean after(Invocation invocation) {
    return doIntercept(invocation);
  }
}

/**
 * 异常拦截
 *
 * @author wubo
 */
class CatchingInterceptor extends AopInterceptor {

  CatchingInterceptor(List<AopWrapper> aopWrapperList) {
    super(aopWrapperList);
  }

  @Override
  public boolean catching(Invocation invocation) {
    return doIntercept(invocation);
  }
}