package vip.justlive.oxygen.core.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.annotation.Before;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;

/**
 * aop插件
 *
 * @author wubo
 */
public class AopPlugin implements Plugin {

  private static final Map<Class<? extends Annotation>, Map<Class<? extends Annotation>, Method>> CACHE = new ConcurrentHashMap<>(
      8, 0.75F);

  @Override
  public int order() {
    return Integer.MIN_VALUE + 10;
  }

  @Override
  public void start() {
    handleBefore();
  }

  void handleBefore() {
    Map<Class<? extends Annotation>, Method> map = new ConcurrentHashMap<>(16, 1);
    CACHE.put(Before.class, map);
    Set<Method> methods = ClassScannerPlugin.getMethodsAnnotatedWith(Before.class);
    for (Method method : methods) {
      Before before = method.getAnnotation(Before.class);
      Class<? extends Annotation> annotation = before.annotation();
      if (annotation != Annotation.class) {
        map.put(annotation, method);
      }
    }
  }
}
