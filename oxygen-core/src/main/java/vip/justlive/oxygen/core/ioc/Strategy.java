package vip.justlive.oxygen.core.ioc;

/**
 * 注入策略
 *
 * @author wubo
 */
public interface Strategy {

  /**
   * 实例对象
   *
   * @param clazz 类
   * @return 实例
   */
  Object instance(Class<?> clazz);

  /**
   * 设置非必须
   */
  void nonRequired();

  /**
   * 是否需要
   *
   * @return
   */
  boolean isRequired();

}
