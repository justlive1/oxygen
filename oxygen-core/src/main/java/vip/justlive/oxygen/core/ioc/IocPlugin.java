package vip.justlive.oxygen.core.ioc;

import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.annotation.Bean;
import vip.justlive.oxygen.core.annotation.Configuration;
import vip.justlive.oxygen.core.scan.ClassScanner;

/**
 * IocPlugin
 * <br>
 * 当前只支持构造方法注入
 * <br>
 * 原因: 易于切换不同ioc实现
 *
 * @author wubo
 */
@Slf4j
public class IocPlugin implements Plugin {

  private static final AtomicInteger TODO_INJECT = new AtomicInteger();

  private static Strategy strategy = new ConstructorStrategy();

  @Override
  public int order() {
    return 0;
  }

  @Override
  public void start() {
    scan();
    ioc();
    merge();
  }

  /**
   * 实例bean， 通过构造函数实例
   *
   * @param clazz 类
   * @return 实例
   */
  Object instanceBean(Class<?> clazz) {
    return strategy.instance(clazz);
  }

  void scan() {
    // Configuration
    for (Class<?> clazz : ClassScanner.getTypesAnnotatedWith(Configuration.class)) {
      configBeans(clazz);
    }
    // Bean
    for (Class<?> clazz : ClassScanner.getTypesAnnotatedWith(Bean.class)) {
      Bean singleton = clazz.getAnnotation(Bean.class);
      String beanName = singleton.value();
      if (beanName == null || beanName.length() == 0) {
        beanName = clazz.getName();
      }
      BeanStore.seize(clazz, beanName);
      TODO_INJECT.incrementAndGet();
    }
  }

  void configBeans(Class<?> clazz) {
    Object obj;
    try {
      obj = clazz.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException(String.format("@Configuration注解的类[%s]无法实例化", clazz), e);
    }
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Bean.class)) {
        if (method.getParameterCount() > 0) {
          throw new IllegalStateException("@Configuration下实例Bean不支持有参方式");
        }
        method.setAccessible(true);
        Object bean;
        try {
          bean = method.invoke(obj);
        } catch (Exception e) {
          throw new IllegalStateException("@Configuration下实例方法出错", e);
        }
        Bean singleton = method.getAnnotation(Bean.class);
        String name = singleton.value();
        if (name.length() == 0) {
          name = method.getName();
        }
        BeanStore.putBean(name, bean);
      }
    }
  }

  void ioc() {
    int pre = TODO_INJECT.get();
    while (TODO_INJECT.get() > 0) {
      instance();
      int now = TODO_INJECT.get();
      if (now > 0 && now == pre) {
        if (!strategy.isRequired()) {
          if (log.isDebugEnabled()) {
            log.debug("ioc失败 出现循环依赖或缺失Bean TODO_INJECT={}, beans={}", now, BeanStore.BEANS);
          }
          throw new IllegalStateException("发生循环依赖或者缺失Bean ");
        } else {
          strategy.nonRequired();
        }
      }
      pre = now;
    }
  }

  void instance() {
    BeanStore.BEANS.forEach((clazz, value) -> value.forEach((name, v) -> {
      if (v == BeanStore.EMPTY) {
        Object inst = instanceBean(clazz);
        if (inst != null) {
          BeanStore.putBean(name, inst);
          TODO_INJECT.decrementAndGet();
        }
      }
    }));
  }

  void merge() {
    for (Entry<Class<?>, ConcurrentMap<String, Object>> entry : BeanStore.BEANS.entrySet()) {
      Class<?> clazz = entry.getKey();
      BeanStore.merge(clazz, entry.getValue().values().iterator().next());
    }
  }

}
