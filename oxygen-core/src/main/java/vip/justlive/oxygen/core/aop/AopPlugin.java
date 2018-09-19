package vip.justlive.oxygen.core.aop;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.annotation.After;
import vip.justlive.oxygen.core.annotation.Before;
import vip.justlive.oxygen.core.annotation.Catching;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;

/**
 * aop插件
 *
 * @author wubo
 */
public class AopPlugin implements Plugin {

  private static final Map<Class<? extends Annotation>, ListMultimap<Class<? extends Annotation>, AopWrapper>> CACHE = new ConcurrentHashMap<>(
      8, 0.75F);
  private static final Map<Class<?>, Object> AOP_CACHE = new ConcurrentHashMap<>(8, 1F);

  @Override
  public int order() {
    return Integer.MIN_VALUE + 10;
  }

  @Override
  public void start() {
    handleBefore();
    handleAfter();
    handleCatching();
  }

  void handleBefore() {
    ListMultimap<Class<? extends Annotation>, AopWrapper> listMultimap = LinkedListMultimap
        .create();
    CACHE.put(Before.class, listMultimap);
    Set<Method> methods = ClassScannerPlugin.getMethodsAnnotatedWith(Before.class);
    for (Method method : methods) {
      Before before = method.getAnnotation(Before.class);
      Class<? extends Annotation> annotation = before.annotation();
      if (annotation != Annotation.class) {
        listMultimap.put(annotation, new AopWrapper(instance(method.getDeclaringClass()), method));
      }
    }
  }

  void handleAfter() {
    ListMultimap<Class<? extends Annotation>, AopWrapper> listMultimap = LinkedListMultimap
        .create();
    CACHE.put(After.class, listMultimap);
    Set<Method> methods = ClassScannerPlugin.getMethodsAnnotatedWith(After.class);
    for (Method method : methods) {
      After after = method.getAnnotation(After.class);
      Class<? extends Annotation> annotation = after.annotation();
      if (annotation != Annotation.class) {
        listMultimap.put(annotation, new AopWrapper(instance(method.getDeclaringClass()), method));
      }
    }
  }

  void handleCatching() {
    ListMultimap<Class<? extends Annotation>, AopWrapper> listMultimap = LinkedListMultimap
        .create();
    CACHE.put(Catching.class, listMultimap);
    Set<Method> methods = ClassScannerPlugin.getMethodsAnnotatedWith(Catching.class);
    for (Method method : methods) {
      Catching catching = method.getAnnotation(Catching.class);
      Class<? extends Annotation> annotation = catching.annotation();
      if (annotation != Annotation.class) {
        listMultimap.put(annotation, new AopWrapper(instance(method.getDeclaringClass()), method));
      }
    }
  }

  /**
   * 获取aop增强方法
   *
   * @param annotation 注解
   * @param targetAnnotation 目标注解
   * @return 增强方法
   */
  public static List<AopWrapper> getAopMethod(Class<? extends Annotation> annotation,
      Class<? extends Annotation> targetAnnotation) {
    ListMultimap<Class<? extends Annotation>, AopWrapper> listMultimap = CACHE.get(annotation);
    if (listMultimap != null) {
      return listMultimap.get(targetAnnotation);
    }
    return null;
  }

  private Object instance(Class<?> clazz) {
    Object obj = AOP_CACHE.get(clazz);
    if (obj != null) {
      return obj;
    }
    try {
      obj = clazz.newInstance();
      AOP_CACHE.putIfAbsent(clazz, obj);
      return AOP_CACHE.get(clazz);
    } catch (InstantiationException | IllegalAccessException e) {
      throw Exceptions.wrap(e);
    }
  }
}
