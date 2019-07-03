/*
 * Copyright (C) 2019 justlive1
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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * objects工具增强类
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class MoreObjects {

  /**
   * 获取第一个不为null的值，没有则返回null
   *
   * @param first first value
   * @param second second value
   * @param <T> 泛型
   * @return nonNull
   */
  public static <T> T firstOrNull(T first, T second) {
    if (first != null) {
      return first;
    }
    return second;
  }

  /**
   * 获取第一个不为null的值
   *
   * @param first first value
   * @param second second value
   * @param <T> 泛型
   * @return nonNull
   */
  public static <T> T firstNonNull(T first, T second) {
    T obj = firstOrNull(first, second);
    if (obj != null) {
      return obj;
    }
    throw new IllegalArgumentException();
  }

  /**
   * 获取第一个不为null或空字符串的值
   *
   * @param first first value
   * @param second second value
   * @param others other values
   * @return nonEmpty
   */
  public static String firstNonEmpty(String first, String second, String... others) {
    if (first != null && first.length() > 0) {
      return first;
    }
    if (second != null && second.length() > 0) {
      return second;
    }
    if (others == null || others.length == 0) {
      throw new IllegalArgumentException();
    }
    for (String str : others) {
      if (str != null && str.length() > 0) {
        return str;
      }
    }
    throw new IllegalArgumentException();
  }

  /**
   * 对象转map
   *
   * @param bean 对象
   * @return map
   */
  public static Map<String, Object> beanToMap(Object bean) {
    Checks.notNull(bean, "bean can not be null");
    Map<String, Object> map = new HashMap<>(4);
    if (Map.class.isAssignableFrom(bean.getClass())) {
      Map<?, ?> beanMap = (Map<?, ?>) bean;
      beanMap.forEach((k, v) -> map.put(k.toString(), v));
    } else {
      for (Field field : ReflectUtils.getAllDeclaredFields(bean.getClass())) {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        field.setAccessible(true);
        try {
          Object value = field.get(bean);
          if (value != null) {
            map.put(field.getName(), value);
          }
        } catch (IllegalAccessException e) {
          log.warn("field can not get value", e);
        }
      }
    }
    return map;
  }

  /**
   * bean转换成queryString
   *
   * @param bean 对象
   * @return queryString
   */
  public static String beanToQueryString(Object bean) {
    return beanToQueryString(bean, false);
  }

  /**
   * bean转换成queryString
   *
   * @param bean 对象
   * @param urlEncoded url encoded
   * @return queryString
   */
  public static String beanToQueryString(Object bean, boolean urlEncoded) {
    Map<String, Object> map = MoreObjects.beanToMap(bean);
    StringBuilder sb = new StringBuilder();
    map.forEach((k, v) -> sb.append(Constants.AND).append(k).append(Constants.EQUAL)
        .append(urlEncoded ? urlEncode(v.toString()) : v));
    if (sb.length() > 0) {
      sb.deleteCharAt(0);
    }
    return sb.toString();
  }

  /**
   * url encode
   *
   * @param s string
   * @return encoded
   */
  public static String urlEncode(String s) {
    return urlEncode(s, StandardCharsets.UTF_8);
  }

  /**
   * url encode
   *
   * @param s string
   * @param charset 字符集
   * @return encoded
   */
  public static String urlEncode(String s, Charset charset) {
    try {
      return URLEncoder.encode(s, charset.name());
    } catch (UnsupportedEncodingException e) {
      // nothing
    }
    return s;
  }

  /**
   * always true
   *
   * @param <T> 泛型
   * @return predicate
   */
  public static <T> Predicate<T> alwaysTrue() {
    return t -> true;
  }

  /**
   * always false
   *
   * @param <T> 泛型
   * @return predicate
   */
  public static <T> Predicate<T> alwaysFalse() {
    return t -> false;
  }

  /**
   * toString
   *
   * @param obj obj
   * @return string
   */
  public static String safeToString(Object obj) {
    if (obj == null) {
      return Constants.EMPTY;
    }
    return obj.toString();
  }
}
