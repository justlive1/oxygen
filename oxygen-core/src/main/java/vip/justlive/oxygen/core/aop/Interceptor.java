package vip.justlive.oxygen.core.aop;

import java.lang.reflect.Method;

/**
 * 拦截接口
 *
 * @author wubo
 */
public interface Interceptor {

  /**
   * 前置增强
   *
   * @param target 目标
   * @param method 方法
   * @param args 参数
   */
  default void before(Object target, Method method, Object[] args) {
  }

  /**
   * 后置增强
   *
   * @param target 目标
   * @param method 方法
   * @param args 参数
   */
  default void after(Object target, Method method, Object[] args) {
  }

  /**
   * 异常增强
   *
   * @param target 目标
   * @param method 方法
   * @param args 参数
   */
  default void catching(Object target, Method method, Object[] args) {
  }
}
