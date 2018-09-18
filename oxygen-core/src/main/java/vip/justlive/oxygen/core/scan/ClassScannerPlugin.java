package vip.justlive.oxygen.core.scan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * 类扫描器
 *
 * @author wubo
 */
public class ClassScannerPlugin implements Plugin {

  private static final Map<String, Reflections> REFS = new HashMap<>(4);

  /**
   * 根据注解获取类
   *
   * @param annotation 注解
   * @return classes
   */
  public static Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation) {
    return REFS.get(Constants.DEFAULT_PROFILE).getTypesAnnotatedWith(annotation);
  }

  /**
   * 根据注解获取方法
   *
   * @param annotation 注解
   * @return methods
   */
  public static Set<Method> getMethodsAnnotatedWith(final Class<? extends Annotation> annotation) {
    return REFS.get(Constants.DEFAULT_PROFILE).getMethodsAnnotatedWith(annotation);
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE;
  }

  @Override
  public void start() {
    String path = ConfigFactory.getProperty(Constants.CLASS_SCAN_KEY);
    Reflections ref;
    if (path == null) {
      ref = new Reflections("vip.justlive", new SubTypesScanner(), new TypeAnnotationsScanner(),
          new MethodAnnotationsScanner());
    } else {
      ref = new Reflections("vip.justlive", path.split(Constants.COMMA), new SubTypesScanner(),
          new TypeAnnotationsScanner(), new MethodAnnotationsScanner());
    }
    REFS.put(Constants.DEFAULT_PROFILE, ref);
  }

  @Override
  public void stop() {
    REFS.clear();
  }
}
