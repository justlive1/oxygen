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
package vip.justlive.oxygen.core.util.base;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * objects工具增强类
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class MoreObjects {

  static final Random RANDOM = new Random();

  /**
   * 非空检查
   *
   * @param obj 校验值
   * @param <T> 泛型类
   * @return 传入值
   */
  public <T> T notNull(T obj) {
    return notNull(obj, "can not be null");
  }

  /**
   * 非空检查
   *
   * @param obj 校验值
   * @param msg 错误信息
   * @param <T> 泛型类
   * @return 传入值
   */
  public <T> T notNull(T obj, String msg) {
    if (obj == null) {
      throw Exceptions.fail(msg);
    }
    return obj;
  }

  /**
   * 获取第一个不为null的值，没有则返回null
   *
   * @param first  first value
   * @param second second value
   * @param others other values
   * @param <T>    泛型
   * @return nonNull
   */
  @SafeVarargs
  public <T> T firstOrNull(T first, T second, T... others) {
    if (first != null) {
      return first;
    }
    if (second != null) {
      return second;
    }
    if (others != null) {
      for (T value : others) {
        if (value != null) {
          return value;
        }
      }
    }
    return null;
  }

  /**
   * 获取第一个不为null的值
   *
   * @param first  first value
   * @param second second value
   * @param others other values
   * @param <T>    泛型
   * @return nonNull
   */
  @SafeVarargs
  public <T> T firstNonNull(T first, T second, T... others) {
    T obj = firstOrNull(first, second, others);
    if (obj != null) {
      return obj;
    }
    throw new IllegalArgumentException();
  }

  /**
   * 对象转map
   *
   * @param bean 对象
   * @return map
   */
  public Map<String, Object> beanToMap(Object bean) {
    return beanToMap(bean, false);
  }

  /**
   * 对象转map 对象属性也同样转换
   *
   * @param bean 对象
   * @param deep 是否深度转换
   * @return map
   */
  public Map<String, Object> beanToMap(Object bean, boolean deep) {
    notNull(bean, "bean can not be null");
    Map<String, Object> map = new HashMap<>(4);
    if (Map.class.isAssignableFrom(bean.getClass())) {
      Map<?, ?> beanMap = (Map<?, ?>) bean;
      beanMap.forEach((k, v) -> convert(map, k.toString(), v, deep));
    } else {
      for (Field field : ClassUtils.getAllDeclaredFields(bean.getClass())) {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        try {
          field.setAccessible(true);
          Object value = field.get(bean);
          if (value != null) {
            convert(map, field.getName(), value, deep);
          }
        } catch (IllegalAccessException e) {
          log.warn("field can not get value", e);
        }
      }
    }
    return map;
  }

  /**
   * 解析queryString转成Map
   *
   * @param queryString qs
   * @return map
   */
  public Map<String, String> parseQueryString(String queryString) {
    Map<String, String> queryMap = new HashMap<>(8);
    if (!Strings.hasText(queryString)) {
      return queryMap;
    }
    String[] params = queryString.split(Strings.AND);
    for (String param : params) {
      String[] p = param.split(Strings.EQUAL);
      if (p.length == 2) {
        queryMap.put(p[0], p[1]);
      }
    }
    return queryMap;
  }

  /**
   * @param queryString
   * @return
   */
  public Map<String, List<String>> parseMultiQueryString(String queryString) {
    Map<String, List<String>> queryMap = new HashMap<>(8);
    if (!Strings.hasText(queryString)) {
      return queryMap;
    }
    String[] params = queryString.split(Strings.AND);
    for (String param : params) {
      String[] p = param.split(Strings.EQUAL);
      if (p.length == 2) {
        queryMap.computeIfAbsent(p[0], k -> new ArrayList<>()).add(p[1]);
      }
    }
    return queryMap;
  }

  /**
   * bean转换成queryString
   *
   * @param bean 对象
   * @return queryString
   */
  public String beanToQueryString(Object bean) {
    return beanToQueryString(bean, false);
  }

  /**
   * bean转换成queryString
   *
   * @param bean       对象
   * @param urlEncoded url encoded
   * @return queryString
   */
  public String beanToQueryString(Object bean, boolean urlEncoded) {
    Map<String, Object> map = MoreObjects.beanToMap(bean);
    StringBuilder sb = new StringBuilder();
    map.forEach((k, v) -> sb.append(Strings.AND).append(k).append(Strings.EQUAL)
        .append(urlEncoded ? Urls.urlEncode(v.toString()) : v));
    if (sb.length() > 0) {
      sb.deleteCharAt(0);
    }
    return sb.toString();
  }

  /**
   * map转换成properties
   *
   * @param bean 数据
   * @return props
   */
  public Properties beanToProps(Object bean) {
    Map<String, Object> map = beanToMap(bean, true);
    Properties props = new Properties();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (entry.getValue() instanceof Map) {
        convert((Map<?, ?>) entry.getValue(), props, entry.getKey().toString());
      } else {
        props.setProperty(entry.getKey().toString(), entry.getValue().toString());
      }
    }
    return props;
  }


  /**
   * always true
   *
   * @param <T> 泛型
   * @return predicate
   */
  public <T> Predicate<T> alwaysTrue() {
    return t -> true;
  }

  /**
   * always false
   *
   * @param <T> 泛型
   * @return predicate
   */
  public <T> Predicate<T> alwaysFalse() {
    return t -> false;
  }

  /**
   * toString
   *
   * @param obj obj
   * @return string
   */
  public String safeToString(Object obj) {
    if (obj == null) {
      return Strings.EMPTY;
    }
    return obj.toString();
  }

  /**
   * 捕获异常的foreach
   *
   * @param iterable 迭代对象
   * @param consumer 处理单元
   * @param <T>      泛型
   */
  public <T> void caughtForeach(Iterable<T> iterable, Consumer<? super T> consumer) {
    if (consumer instanceof CaughtConsumer) {
      iterable.forEach(consumer);
    } else {
      iterable.forEach(new CaughtConsumer<>(consumer));
    }
  }

  /**
   * 创建map
   *
   * @param <K> 泛型
   * @param <V> 泛型
   * @return map
   */
  public <K, V> Map<K, V> mapOf() {
    return new HashMap<>(2);
  }

  /**
   * 创建map
   *
   * @param k   键
   * @param v   值
   * @param <K> 泛型
   * @param <V> 泛型
   * @return map
   */
  public <K, V> Map<K, V> mapOf(K k, V v) {
    Map<K, V> map = mapOf();
    map.put(notNull(k), v);
    return map;
  }

  /**
   * 创建map
   *
   * @param k1  第一个键
   * @param v1  第一个值
   * @param k2  第二个键
   * @param v2  第二个值
   * @param <K> 泛型
   * @param <V> 泛型
   * @return map
   */
  public <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
    Map<K, V> map = mapOf();
    map.put(notNull(k1), v1);
    map.put(notNull(k2), v2);
    return map;
  }

  /**
   * 创建map
   *
   * @param k1  第一个键
   * @param v1  第一个值
   * @param k2  第二个键
   * @param v2  第二个值
   * @param k3  第三个键
   * @param v3  第三个值
   * @param <K> 泛型
   * @param <V> 泛型
   * @return map
   */
  public <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
    Map<K, V> map = mapOf();
    map.put(notNull(k1), v1);
    map.put(notNull(k2), v2);
    map.put(notNull(k3), v3);
    return map;
  }

  /**
   * 取交集
   *
   * @param first  第一个集合
   * @param second 第二个集合
   * @param others 其他集合
   * @param <T>    泛型
   * @return 交集
   */
  @SafeVarargs
  public <T> Set<T> intersection(Set<T> first, Set<T> second, Set<T>... others) {
    Set<T> intersection = new HashSet<>();
    for (T obj : first) {
      if (second.contains(obj)) {
        intersection.add(obj);
      }
    }
    if (others == null) {
      return intersection;
    }
    for (Set<T> other : others) {
      intersection.retainAll(other);
    }
    return intersection;
  }

  /**
   * 去并集
   *
   * @param first  第一个集合
   * @param second 第二个集合
   * @param others 其他集合
   * @param <T>    泛型
   * @return 并集
   */
  @SafeVarargs
  public <T> Set<T> union(Set<T> first, Set<T> second, Set<T>... others) {
    Set<T> union = new HashSet<>(first);
    union.addAll(second);
    if (others == null) {
      return union;
    }
    for (Set<T> other : others) {
      union.addAll(other);
    }
    return union;
  }

  /**
   * 当前线程直接运行
   *
   * @return executor
   */
  public Executor directExecutor() {
    return Runnable::run;
  }

  private void convert(Map<?, ?> map, Properties result, String prefix) {
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (entry.getValue() instanceof Map) {
        convert((Map<?, ?>) entry.getValue(), result,
            prefix + Strings.DOT + entry.getKey().toString());
      } else {
        result.setProperty(prefix + Strings.DOT + entry.getKey().toString(),
            entry.getValue().toString());
      }
    }
  }

  private void convert(Map<String, Object> result, String key, Object value, boolean deep) {
    if (!deep) {
      result.put(key, value);
      return;
    }

    if (ClassUtils.isJavaInternalType(value.getClass()) && !(value instanceof Map)) {
      result.put(key, value);
      return;
    }
    result.put(key, beanToMap(value, true));
  }
}
