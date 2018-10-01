/*
 * Copyright (C) 2018 justlive1
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 反射相关工具类
 *
 * @author wubo
 */
@Slf4j
public class ReflectUtils {

  public static final String MODULE_VALID = "REFLECT";
  public static final String INVAID_CLASS = "10000";

  private ReflectUtils() {
  }

  /**
   * 获取class
   *
   * @param className 类名
   * @return 类
   */
  public static Class<?> forName(String className) {
    Class<?> clazz = null;
    ClassLoader threadContextClassLoader = Thread.currentThread().getContextClassLoader();
    if (threadContextClassLoader != null) {
      try {
        clazz = threadContextClassLoader.loadClass(className);
      } catch (ClassNotFoundException e) {
        log.warn(e.getMessage(), e);
      }
    }

    if (clazz == null) {
      try {
        clazz = ReflectUtils.class.getClassLoader().loadClass(className);
      } catch (ClassNotFoundException e) {
        log.warn(e.getMessage(), e);
      }
    }

    if (clazz == null) {
      throw Exceptions.fail(INVAID_CLASS, "Failed to load driver class " + className
          + " in either of DataSourceUtils class loader or Thread context classloader");
    }

    return clazz;
  }

  /**
   * 实例化对象
   *
   * @param className 类名
   * @param <T> 泛型类
   * @return 实例化对象
   */
  @SuppressWarnings("unchecked")
  public static <T> T fromName(String className) {
    Class<?> clazz = forName(className);

    try {
      return (T) newInstance(clazz);
    } catch (Exception e) {
      throw Exceptions.wrap(e, INVAID_CLASS, "Failed to instantiate class " + className);
    }

  }

  /**
   * 获取当前Class类实例中所有的Field对象（包括父类的Field）
   *
   * @param clazz Class类实例
   * @return 对象类型数组
   */
  public static Field[] getAllDeclaredFields(Class<?> clazz) {
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
  public static Field getDeclaredField(Class<?> clazz, String propertyName) {
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
  public static Object getValue(Object object, String propertyName) {
    Class<?> clazz = object.getClass();
    Field field = getDeclaredField(clazz, propertyName);
    if (field == null) {
      return null;
    }
    if (!field.isAccessible()) {
      field.setAccessible(true);
    }
    try {
      return field.get(object);
    } catch (Exception e) {
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
  public static void setValue(Object object, Field field, Object value) {
    field.setAccessible(true);
    try {
      field.set(object, value);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      log.error("set value {} to class {} error", value, object.getClass(), e);
    }
  }

  /**
   * 把传入对象的非空对象的值赋给持久化对象
   *
   * @param persistentObject 持久化对象
   * @param newObject 传入的对象
   * @throws IllegalAccessException 访问非法异常
   * @throws IllegalArgumentException 非法逻辑异常
   */
  public static void replaceNullProperty(Object persistentObject, Object newObject)
      throws IllegalAccessException {
    Class<?> clazz = persistentObject.getClass();
    Field[] fields = getAllDeclaredFields(clazz);
    for (int i = 0; i < fields.length; i++) {
      if (!fields[i].isAccessible()) {
        fields[i].setAccessible(true);
      }
      Object fieldContent = fields[i].get(newObject);
      if (fieldContent != null && !Modifier.isFinal(fields[i].getModifiers())) {
        fields[i].set(persistentObject, fieldContent);
      }
    }
  }

  /**
   * class中是否存在指定的方法
   *
   * @param clazz CLASS类型
   * @param methodName 方法名
   * @return boolean 否存在指定的方法中
   */
  public static boolean isContainMethod(Class<?> clazz, String methodName) {
    Method[] methods = clazz.getMethods();
    for (int i = 0; i < methods.length; i++) {
      if (methods[i].getName().equals(methodName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * clone一个Object使用序列号的形式
   *
   * @param src 需要序列化的对象
   * @return 拷贝对象
   */
  public static Object byteClone(Object src) {

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(baos);
      out.writeObject(src);
      out.close();
      ByteArrayInputStream bin = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream in = new ObjectInputStream(bin);
      Object clone = in.readObject();
      in.close();
      return clone;
    } catch (Exception ex) {
      throw Exceptions.wrap(ex);
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
  public static <T> T newInstance(Class<T> clazz) {
    if (clazz == Integer.class) {
      return (T) Integer.valueOf(0);
    }
    if (clazz == String.class) {
      return (T) "";
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
      return clazz.newInstance();
    } catch (Exception e) {
      throw Exceptions
          .wrap(e, INVAID_CLASS, "Can NOT instance new object for class [" + clazz + "]");
    }
  }

  public static Field[] getInheritanceDeclaredFields(Class<?> clazz) {
    Collection<Field> fds = new ArrayList<>();
    Class<?> local = clazz;
    while (true) {
      if (local.isPrimitive() || local == Object.class) {
        break;
      }
      Collections.addAll(fds, local.getDeclaredFields());
      local = local.getSuperclass();
    }
    Field[] ret = new Field[fds.size()];
    fds.toArray(ret);
    fds.clear();
    return ret;
  }

}
