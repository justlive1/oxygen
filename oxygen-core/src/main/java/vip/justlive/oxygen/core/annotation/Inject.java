package vip.justlive.oxygen.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动注入
 * <br>
 * 默认使用类型注入
 * <br>
 * 只支持在构造方法上使用，便于切换IOC框架
 *
 * @author wubo
 */
@Target({ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Inject {

  /**
   * 声明bean是否必须存在
   *
   * @return true为必须存在，否则抛出异常
   */
  boolean required() default true;

}
