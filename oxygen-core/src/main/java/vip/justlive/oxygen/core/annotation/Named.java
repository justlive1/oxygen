package vip.justlive.oxygen.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在构造函数参数上，用于指定注入bean的名称
 *
 * @author wubo
 * @see Inject
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Named {

  /**
   * bean id
   *
   * @return bean的id
   */
  String value();
}
