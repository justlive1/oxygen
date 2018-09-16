package vip.justlive.oxygen.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记路由文件
 *
 * @author wubo
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Route {

  /**
   * route的访问路径
   *
   * @return path
   */
  String value() default "";
}
