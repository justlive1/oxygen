/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package vip.justlive.oxygen.core.util;


import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * class工具
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class ClassUtils {

  /**
   * 数组类名前缀: "[]"
   */
  public final String ARRAY_SUFFIX = "[]";
  /**
   * 内部数组类名前缀: "["
   */
  public final String INTERNAL_ARRAY_PREFIX = "[";
  /**
   * 内部非基本数组类名前缀: "[L"
   */
  public final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

  private final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_TYPE;
  private final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPE;

  static {
    Map<Class<?>, Class<?>> primToWrap = new HashMap<>(16);
    Map<Class<?>, Class<?>> wrapToPrim = new HashMap<>(16);

    add(primToWrap, wrapToPrim, boolean.class, Boolean.class);
    add(primToWrap, wrapToPrim, byte.class, Byte.class);
    add(primToWrap, wrapToPrim, char.class, Character.class);
    add(primToWrap, wrapToPrim, double.class, Double.class);
    add(primToWrap, wrapToPrim, float.class, Float.class);
    add(primToWrap, wrapToPrim, int.class, Integer.class);
    add(primToWrap, wrapToPrim, long.class, Long.class);
    add(primToWrap, wrapToPrim, short.class, Short.class);
    add(primToWrap, wrapToPrim, void.class, Void.class);

    PRIMITIVE_TO_WRAPPER_TYPE = Collections.unmodifiableMap(primToWrap);
    WRAPPER_TO_PRIMITIVE_TYPE = Collections.unmodifiableMap(wrapToPrim);
  }

  private void add(Map<Class<?>, Class<?>> forward, Map<Class<?>, Class<?>> backward,
      Class<?> key, Class<?> value) {
    forward.put(key, value);
    backward.put(value, key);
  }

  /**
   * 所有基本类型
   *
   * @return 基本类型集合
   */
  public Set<Class<?>> allPrimitiveTypes() {
    return PRIMITIVE_TO_WRAPPER_TYPE.keySet();
  }

  /**
   * 基本类型包装类
   *
   * @return 基本类型包装类集合
   */
  public Set<Class<?>> allWrapperTypes() {
    return WRAPPER_TO_PRIMITIVE_TYPE.keySet();
  }

  /**
   * 获取包装类型
   *
   * @param type 类型
   * @param <T> 泛型
   * @return 包装类
   */
  public <T> Class<T> wrap(Class<T> type) {
    MoreObjects.notNull(type);
    // cast is safe: long.class and Long.class are both of type Class<Long>
    @SuppressWarnings("unchecked") Class<T> wrapped = (Class<T>) PRIMITIVE_TO_WRAPPER_TYPE
        .get(type);
    return (wrapped == null) ? type : wrapped;
  }

  /**
   * 获取包装类的基本类型
   *
   * @param type 类型
   * @param <T> 泛型
   * @return 基本类型
   */
  public <T> Class<T> unwrap(Class<T> type) {
    MoreObjects.notNull(type);
    // cast is safe: long.class and Long.class are both of type Class<Long>
    @SuppressWarnings("unchecked") Class<T> unwrapped = (Class<T>) WRAPPER_TO_PRIMITIVE_TYPE
        .get(type);
    return (unwrapped == null) ? type : unwrapped;
  }

  /**
   * 是否为基本类型或包装类
   *
   * @param type 类型
   * @return true则为基本类型或包装类
   */
  public boolean isPrimitive(Class<?> type) {
    return PRIMITIVE_TO_WRAPPER_TYPE.containsKey(type) || WRAPPER_TO_PRIMITIVE_TYPE
        .containsKey(type);
  }

  /**
   * 获取默认类加载器
   *
   * @return classloader
   */
  public ClassLoader getDefaultClassLoader() {
    ClassLoader cl = null;
    try {
      cl = Thread.currentThread().getContextClassLoader();
    } catch (Exception ex) {
      // 线程上下文加载不了
    }
    if (cl == null) {
      // 使用类加载器
      cl = ClassUtils.class.getClassLoader();
      if (cl == null) {
        // 获取系统类加载器
        try {
          cl = ClassLoader.getSystemClassLoader();
        } catch (Exception ex) {
          // ...
        }
      }
    }
    return cl;
  }

  /**
   * 获取Class实例
   *
   * @param name 类名
   * @return class
   */
  public Class<?> forName(String name) {
    return forName(name, getDefaultClassLoader());
  }

  /**
   * 获取Class实例
   *
   * @param name 类名
   * @param classLoader 类加载器
   * @return class
   */
  public Class<?> forName(String name, ClassLoader classLoader) {
    // "java.lang.String[]" style arrays
    if (name.endsWith(ARRAY_SUFFIX)) {
      String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
      Class<?> elementClass = forName(elementClassName, classLoader);
      return Array.newInstance(elementClass, 0).getClass();
    }

    // "[Ljava.lang.String;" style arrays
    if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(Strings.SEMICOLON)) {
      String elementName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
      Class<?> elementClass = forName(elementName, classLoader);
      return Array.newInstance(elementClass, 0).getClass();
    }

    // "[[I" or "[[Ljava.lang.String;" style arrays
    if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
      String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
      Class<?> elementClass = forName(elementName, classLoader);
      return Array.newInstance(elementClass, 0).getClass();
    }

    ClassLoader clToUse = classLoader;
    if (clToUse == null) {
      clToUse = getDefaultClassLoader();
    }
    try {
      return (clToUse != null ? clToUse.loadClass(name) : Class.forName(name));
    } catch (ClassNotFoundException ex) {
      int lastDotIndex = name.lastIndexOf(Strings.DOT);
      if (lastDotIndex != -1) {
        String innerClassName =
            name.substring(0, lastDotIndex) + Strings.DOLLAR + name.substring(lastDotIndex + 1);
        try {
          return (clToUse != null ? clToUse.loadClass(innerClassName)
              : Class.forName(innerClassName));
        } catch (ClassNotFoundException ex2) {
          // Swallow - let original exception get through
        }
      }
      throw Exceptions.wrap(ex);
    }
  }

  /**
   * 类是否存在
   *
   * @param className 类名
   * @return true为存在
   */
  public boolean isPresent(String className) {
    return isPresent(className, getDefaultClassLoader());
  }

  /**
   * 类是否存在
   *
   * @param className 类名
   * @param classLoader 类加载器
   * @return true为存在
   */
  public boolean isPresent(String className, ClassLoader classLoader) {
    try {
      forName(className, classLoader);
      return true;
    } catch (Exception ex) {
      // class的依赖不存在.
      return false;
    }
  }

  /**
   * 验证类是否是cglib代理对象
   *
   * @param object the object to check
   * @return true为是代理对象
   */
  public boolean isCglibProxy(Object object) {
    return isCglibProxyClass(object.getClass());
  }

  /**
   * 验证类是否是cglib生成类
   *
   * @param clazz the class to check
   * @return true为是代理类
   */
  public boolean isCglibProxyClass(Class<?> clazz) {
    return (clazz != null && isCglibProxyClassName(clazz.getName()));
  }

  /**
   * 验证类名是否为cglib代理类名
   *
   * @param className the class name to check
   * @return true为是代理类
   */
  public boolean isCglibProxyClassName(String className) {
    return (className != null && className.contains(Strings.DOUBLE_DOLLAR));
  }

  /**
   * 获取cglib代理的真实类
   *
   * @param clazz 代理类
   * @return 类
   */
  public Class<?> getCglibActualClass(Class<?> clazz) {
    Class<?> actualClass = clazz;
    while (isCglibProxyClass(actualClass)) {
      actualClass = actualClass.getSuperclass();
    }
    return actualClass;
  }

  /**
   * 获取类下被注解的方法
   *
   * @param clazz 类
   * @param annotation 注解
   * @return methods
   */
  public Set<Method> getMethodsAnnotatedWith(Class<?> clazz,
      Class<? extends Annotation> annotation) {
    Set<Method> methods = new HashSet<>();
    Class<?> actualClass = getCglibActualClass(clazz);
    if (actualClass == null) {
      return methods;
    }
    for (Method method : getAllMethods(actualClass)) {
      if (method.isAnnotationPresent(annotation)) {
        methods.add(method);
      }
    }
    return methods;
  }

  /**
   * 执行方法
   *
   * @param method 方法
   * @param bean 对象
   * @param args 参数
   * @return 结果
   */
  public Object methodInvoke(Method method, Object bean, Object... args) {
    try {
      method.setAccessible(true);
      return method.invoke(bean, args);
    } catch (IllegalAccessException e) {
      throw Exceptions.wrap(e);
    } catch (InvocationTargetException e) {
      throw Exceptions.wrap(e.getTargetException());
    }
  }

  /**
   * 获取类所有方法
   *
   * @param clazz 类
   * @return 方法
   */
  public Set<Method> getAllMethods(Class<?> clazz) {
    Set<Method> methods = new HashSet<>();
    Class<?> actualClass = clazz;
    do {
      methods.addAll(Arrays.asList(actualClass.getDeclaredMethods()));
      actualClass = actualClass.getSuperclass();
    } while (actualClass != null);
    return methods;
  }

  /**
   * 获取构造参数类型
   *
   * @param clazz 类
   * @param args 参数
   * @return class[]
   */
  public Class<?>[] getConstructorArgsTypes(Class<?> clazz, Object... args) {
    for (Constructor<?> constructor : clazz.getConstructors()) {
      if (constructor.getParameterCount() != args.length) {
        continue;
      }
      Class<?>[] paramsTypes = constructor.getParameterTypes();
      boolean matched = true;
      for (int i = 0; i < args.length; i++) {
        if (!paramsTypes[i].isAssignableFrom(args[i].getClass())) {
          matched = false;
          break;
        }
      }
      if (matched) {
        return paramsTypes;
      }
    }
    return new Class<?>[args.length];
  }

  /**
   * 获取被注解的构造方法
   *
   * @param clazz 类
   * @param annotation 注解
   * @return Constructor
   */
  @SuppressWarnings("squid:S1452")
  public Constructor<?> getConstructorAnnotatedWith(Class<?> clazz,
      Class<? extends Annotation> annotation) {
    return getConstructorAnnotatedWith(clazz.getConstructors(), annotation);
  }

  /**
   * 获取被注解的构造方法
   *
   * @param constructors 构造方法
   * @param annotation 注解
   * @return Constructor
   */
  @SuppressWarnings("squid:S1452")
  public Constructor<?> getConstructorAnnotatedWith(Constructor<?>[] constructors,
      Class<? extends Annotation> annotation) {
    for (Constructor<?> constructor : constructors) {
      if (constructor.isAnnotationPresent(annotation)) {
        return constructor;
      }
    }
    return null;
  }

  /**
   * 是否是jdk内置注解
   *
   * @param annotation 注解类
   * @return true为内置注解类
   */
  public boolean isInJavaLangAnnotationPackage(Class<? extends Annotation> annotation) {
    return (annotation != null && annotation.getName().startsWith("java.lang.annotation"));
  }

  /**
   * 是否存在注解
   *
   * @param clazz 类型
   * @param annotation 注解
   * @return true表示存在注解
   */
  public boolean isAnnotationPresent(Class<?> clazz,
      Class<? extends Annotation> annotation) {
    return getAnnotation(clazz, annotation) != null;
  }

  /**
   * 获取注解 支持派生注解
   *
   * @param clazz 类型
   * @param annotation 注解
   * @param <A> 泛型
   * @return 注解
   */
  @SuppressWarnings("unchecked")
  public <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotation) {
    A anno = clazz.getAnnotation(annotation);
    if (anno != null) {
      return anno;
    }
    Annotation[] annotations = clazz.getAnnotations();
    Set<Annotation> visited = new LinkedHashSet<>();
    for (Annotation ann : annotations) {
      recursivelyCollectAnnotations(visited, ann);
    }
    if (visited.isEmpty()) {
      return null;
    }

    for (Annotation ann : visited) {
      if (ann.annotationType() == annotation) {
        return (A) ann;
      }
    }
    return null;
  }

  /**
   * 获取当前Class类实例中所有的Field对象（包括父类的Field）
   *
   * @param clazz Class类实例
   * @return 对象类型数组
   */
  public Field[] getAllDeclaredFields(Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();
    Class<?> superClass = clazz.getSuperclass();
    if (superClass == null) {
      return fields;
    }
    Field[] superFields = getAllDeclaredFields(superClass);
    Field[] destFields = new Field[fields.length + superFields.length];
    System.arraycopy(fields, 0, destFields, 0, fields.length);
    System.arraycopy(superFields, 0, destFields, fields.length, superFields.length);

    return destFields;
  }

  /**
   * 根据属性名，在当前Class类实例的所有Field对象中（包括父类的Field）检索对应的属性值
   *
   * @param clazz Class类实例
   * @param propertyName 属性名
   * @return Field 对象
   */
  public Field getDeclaredField(Class<?> clazz, String propertyName) {
    Field[] fields = getAllDeclaredFields(clazz);
    for (Field field : fields) {
      if (field.getName().equals(propertyName)) {
        return field;
      }
    }
    return null;
  }

  /**
   * 根据属性名来获取属性的值
   *
   * @param object 需要得到属性值的对象
   * @param propertyName 属性名
   * @return Object 属性值
   */
  public Object getValue(Object object, String propertyName) {
    Class<?> clazz = object.getClass();
    return getValue(object, getDeclaredField(clazz, propertyName));
  }

  /**
   * 获取属性值
   *
   * @param object 需要得到属性值的对象
   * @param field 字段
   * @return Object 属性值
   */
  public Object getValue(Object object, Field field) {
    if (field == null) {
      return null;
    }
    try {
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      return field.get(object);
    } catch (IllegalAccessException e) {
      throw Exceptions.wrap(e);
    }
  }

  /**
   * 根据field设置值
   *
   * @param object 目标对象
   * @param field 属性
   * @param value 值
   */
  public void setValue(Object object, Field field, Object value) {
    try {
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      field.set(object, value);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      log.error("set value {} to class {} error", value, object.getClass(), e);
    }
  }

  /**
   * 实例化一个类型为clazz的对象。
   *
   * @param <T> 实例化对象类型
   * @param clazz 实例化类型
   * @return 实例化的对象
   */
  @SuppressWarnings("unchecked")
  public <T> T newInstance(Class<T> clazz) {
    if (clazz == Integer.class) {
      return (T) Integer.valueOf(0);
    }
    if (clazz == String.class) {
      return (T) Strings.EMPTY;
    }
    if (clazz == Long.class) {
      return (T) Long.valueOf(0);
    }
    if (clazz == Short.class) {
      return (T) Short.valueOf((short) 0);
    }
    if (clazz == Byte.class) {
      return (T) Byte.valueOf((byte) 0);
    }
    if (clazz == Float.class) {
      return (T) Float.valueOf(0.0f);
    }
    if (clazz == Double.class) {
      return (T) Double.valueOf(0);
    }
    if (clazz == Boolean.class) {
      return (T) Boolean.FALSE;
    }
    if (clazz == BigDecimal.class) {
      return (T) BigDecimal.ZERO;
    }
    if (clazz == java.sql.Date.class) {
      return (T) new java.sql.Date(System.currentTimeMillis());
    }
    if (clazz == java.sql.Timestamp.class) {
      return (T) new java.sql.Timestamp(System.currentTimeMillis());
    }
    if (clazz == java.util.Map.class) {
      return (T) new HashMap<>(16);
    }
    try {
      return clazz.getConstructor().newInstance();
    } catch (Exception e) {
      throw Exceptions.wrap(e, String.format("can not instance new object for class [%s]", clazz));
    }
  }

  /**
   * 是否java内置类
   *
   * @param type 类型
   * @return true为内置类
   */
  public boolean isJavaInternalType(Class<?> type) {
    if (type != null) {
      return type.getClassLoader() == null;
    }
    return false;
  }

  private void recursivelyCollectAnnotations(Set<Annotation> visited,
      Annotation annotation) {
    Class<? extends Annotation> annotationType = annotation.annotationType();
    if (annotationType == null || isInJavaLangAnnotationPackage(annotationType) || !Modifier
        .isPublic(annotationType.getModifiers()) || !visited.add(annotation)) {
      return;
    }

    for (Annotation metaAnnotation : annotationType.getAnnotations()) {
      recursivelyCollectAnnotations(visited, metaAnnotation);
    }
  }
}
