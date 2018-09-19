package vip.justlive.oxygen.core.aop;

import java.lang.reflect.Method;
import lombok.Data;

/**
 * aop包装
 *
 * @author wubo
 */
@Data
public class AopWrapper {

  private final Object target;
  private final Method method;
}
