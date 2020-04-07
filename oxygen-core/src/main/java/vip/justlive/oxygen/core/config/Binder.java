/*
 * Copyright (C) 2019 the original author or authors.
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

package vip.justlive.oxygen.core.config;

import java.lang.reflect.Field;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.convert.DefaultConverterService;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.io.PropertySource;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.Strings;

/**
 * 属性绑定器
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class Binder {

  private final PropertySource source;

  /**
   * 绑定属性到类
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return obj
   */
  public <T> T bind(Class<T> clazz) {
    return bind(clazz, Strings.DOT);
  }

  /**
   * 绑定指定前缀属性到类
   *
   * @param clazz 类型
   * @param prefix 前缀
   * @param <T> 泛型
   * @return obj
   */
  public <T> T bind(Class<T> clazz, String prefix) {
    T obj;
    try {
      obj = clazz.getConstructor().newInstance();
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }

    Class<?> actualClass = ClassUtils.getCglibActualClass(clazz);
    boolean b = (prefix == null || Strings.DOT.equals(prefix)) && actualClass
        .isAnnotationPresent(ValueConfig.class);
    if (b) {
      prefix = actualClass.getAnnotation(ValueConfig.class).value();
    }
    return clazz.cast(bind(obj, prefix));
  }

  /**
   * 绑定属性到对象实例
   *
   * @param obj 对象实例
   * @param prefix 属性前缀
   * @param <T> 泛型
   * @return obj
   */
  public <T> T bind(T obj, String prefix) {
    if (!Strings.hasText(prefix) || Strings.DOT.equals(prefix)) {
      prefix = null;
    }
    for (Field field : ClassUtils.getAllDeclaredFields(obj.getClass())) {
      if (field.isAnnotationPresent(Ignore.class)) {
        continue;
      }
      String key = field.getName();
      boolean wrap = false;
      if (field.isAnnotationPresent(Value.class)) {
        key = field.getAnnotation(Value.class).value();
        wrap = true;
      } else if (prefix != null) {
        key = prefix + Strings.DOT + key;
      }
      Object value = getProperty(key, field.getType(), wrap);
      if (value != null) {
        ClassUtils.setValue(obj, field, value);
      } else if (source.containPrefix(key + Strings.DOT) && !ClassUtils
          .isJavaInternalType(field.getType())) {
        value = ClassUtils.getValue(obj, field);
        if (value == null) {
          value = bind(field.getType(), key);
        } else {
          bind(value, key);
        }
        ClassUtils.setValue(obj, field, value);
      }
    }
    return obj;
  }

  private Object getProperty(String key, Class<?> type, boolean wrap) {
    String value;
    if (wrap) {
      value = source.getPlaceholderProperty(key);
    } else {
      value = source.getProperty(key);
    }
    if (value == null || value.getClass() == type) {
      return value;
    }
    return DefaultConverterService.sharedConverterService().convert(value, type);
  }

}
