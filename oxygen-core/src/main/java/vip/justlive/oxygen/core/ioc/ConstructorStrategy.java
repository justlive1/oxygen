package vip.justlive.oxygen.core.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.concurrent.ConcurrentMap;
import vip.justlive.oxygen.core.annotation.Inject;
import vip.justlive.oxygen.core.annotation.Named;
import vip.justlive.oxygen.core.aop.CglibProxy;

/**
 * 构造方法策略
 *
 * @author wubo
 */
public class ConstructorStrategy implements Strategy {

  private volatile boolean require = true;

  @Override
  public Object instance(Class<?> clazz) {
    Constructor<?>[] constructors = clazz.getConstructors();
    for (Constructor<?> constructor : constructors) {
      if (constructor.isAnnotationPresent(Inject.class)) {
        return dependencyInstance(clazz, constructor);
      }
    }
    return nonDependencyInstance(clazz);
  }

  Object nonDependencyInstance(Class<?> clazz) {
      return CglibProxy.proxy(clazz);
  }

  Object dependencyInstance(Class<?> clazz, Constructor<?> constructor) {
    Inject inject = constructor.getAnnotation(Inject.class);
    Parameter[] params = constructor.getParameters();
    Object[] args = new Object[params.length];
    boolean canInst = fillParams(params, args, inject.required());
    if (canInst) {
        return CglibProxy.proxy(clazz, args);
    }
    return null;
  }

  Object getVal(Parameter param, ConcurrentMap<String, Object> map) {
    Object val;
    if (param.isAnnotationPresent(Named.class)) {
      Named named = param.getAnnotation(Named.class);
      val = map.get(named.value());
    } else {
      val = map.get(param.getType().getName());
      if (val == null && !map.isEmpty()) {
        val = map.values().iterator().next();
      }
    }
    return val;
  }

  boolean fillParams(Parameter[] params, Object[] args, boolean required) {
    for (int i = 0; i < params.length; i++) {
      ConcurrentMap<String, Object> map = BeanStore.BEANS.get(params[i].getType());
      if (map != null) {
        args[i] = getVal(params[i], map);
      }
      boolean notInst =
          (args[i] == BeanStore.EMPTY) || (require && args[i] == null) || (args[i] == null
              && required);
      if (notInst) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void nonRequired() {
    require = false;
  }

  @Override
  public boolean isRequired() {
    return require;
  }
}
