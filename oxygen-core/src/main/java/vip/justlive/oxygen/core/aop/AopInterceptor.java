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

  protected void doIntercept(Invocation invocation) {
    try {
      for (AopWrapper aopWrapper : aopWrapperList) {
        int count = aopWrapper.getMethod().getParameterCount();
        if (count == 0) {
          aopWrapper.getMethod().invoke(aopWrapper.getTarget());
        } else if (count == 1) {
          if (aopWrapper.getMethod().getParameterTypes()[0] != Invocation.class) {
            throw new IllegalArgumentException("aop方法入参只能为[Invocation]类型");
          }
          aopWrapper.getMethod().invoke(aopWrapper.getTarget(), invocation);
        } else {
          throw new IllegalArgumentException("aop方法入参个数不能大于1");
        }
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw Exceptions.wrap(e);
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
  public void before(Invocation invocation) {
    doIntercept(invocation);
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
  public void after(Invocation invocation) {
    doIntercept(invocation);
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
  public void catching(Invocation invocation) {
    doIntercept(invocation);
  }
}