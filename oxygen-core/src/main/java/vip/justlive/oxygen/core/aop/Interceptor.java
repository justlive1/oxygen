package vip.justlive.oxygen.core.aop;

/**
 * 拦截接口
 *
 * @author wubo
 */
public interface Interceptor {

  /**
   * 前置增强
   *
   * @param invocation 调用封装
   */
  default void before(Invocation invocation) {
  }

  /**
   * 后置增强
   *
   * @param invocation 调用封装
   */
  default void after(Invocation invocation) {
  }

  /**
   * 异常增强
   *
   * @param invocation 调用封装
   */
  default void catching(Invocation invocation) {
  }
}
