package vip.justlive.oxygen.core.annotation;

/**
 * 自动创建单例bean
 * <br>
 * 可用在类上用于自动实例化
 * <br>
 * 也可用于Configuration类的方法上创建实例
 *
 * @author wubo
 */
public @interface Bean {

  /**
   * bean的id，默认使用class::getName
   *
   * @return bean的id
   */
  String value() default "";
}
