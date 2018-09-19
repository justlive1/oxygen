package vip.justlive.oxygen.core.aop;

import java.lang.reflect.Method;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * aop调用封装
 *
 * @author wubo
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public final class Invocation {

  @NonNull
  private Object target;
  @NonNull
  private Method method;
  @NonNull
  private Object[] args;
  private Object returnValue;

}
