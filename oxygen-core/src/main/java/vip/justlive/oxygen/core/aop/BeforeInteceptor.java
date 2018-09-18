package vip.justlive.oxygen.core.aop;

import java.lang.reflect.Method;
import vip.justlive.oxygen.core.annotation.Before;

/**
 * 前置切面
 *
 * @author wubo
 */
public class BeforeInteceptor implements Interceptor {

  private final Before before;

  public BeforeInteceptor(Before before) {
    this.before = before;
  }

  @Override
  public void before(Object target, Method method, Object[] args) {
    System.out.println(1);
  }
}
