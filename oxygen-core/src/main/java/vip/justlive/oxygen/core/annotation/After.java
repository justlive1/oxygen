package vip.justlive.oxygen.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 后置切面
 *
 * @author wubo
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface After {

  /**
   * 增强的注解
   *
   * @return annotation
   */
  Class<? extends Annotation> annotation() default Annotation.class;
}
