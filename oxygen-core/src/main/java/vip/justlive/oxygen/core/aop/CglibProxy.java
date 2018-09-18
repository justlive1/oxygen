package vip.justlive.oxygen.core.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * cglib代理
 *
 * @author wubo
 */
public class CglibProxy implements MethodInterceptor {

  private static final CglibProxy CGLIB_PROXY = new CglibProxy();
  private static final Map<Method, List<Interceptor>> CACHE = new ConcurrentHashMap<>(16, 1);

  /**
   * 代理
   *
   * @param targetClass 目标
   * @param <T> 泛型
   * @return 代理对象
   */
  public static <T> T proxy(Class<T> targetClass) {
    return targetClass.cast(Enhancer.create(targetClass, CGLIB_PROXY));
  }

  /**
   * 代理
   *
   * @param targetClass 目标
   * @param args 参数
   * @param <T> 泛型
   * @return 代理对象
   */
  public static <T> T proxy(Class<T> targetClass, Object... args) {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(targetClass);
    enhancer.setCallback(CGLIB_PROXY);
    Class<?>[] classes = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      classes[i] = args[i].getClass();
    }
    return targetClass.cast(enhancer.create(classes, args));
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy)
      throws Throwable {
    List<Interceptor> interceptors = CACHE.get(method);
    if (interceptors == null) {
      List<Interceptor> list = new ArrayList<>();
      // TODO
      CACHE.putIfAbsent(method, list);
    }
    interceptors = CACHE.get(method);
    int index = 0, len = interceptors.size();
    for (; index < len; index++) {
      interceptors.get(index).before(obj, method, args);
    }
    Object returnValue = methodProxy.invokeSuper(obj, args);
    for (; len > 0 && index >= 0; index--) {
      interceptors.get(index).after(obj, method, args);
    }
    return returnValue;
  }

}
