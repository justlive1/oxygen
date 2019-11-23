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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import vip.justlive.oxygen.core.convert.DefaultConverterService;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.io.PropertiesLoader;
import vip.justlive.oxygen.core.io.PropertySource;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.Strings;

/**
 * 配置工厂
 *
 * @author wubo
 */
public class ConfigFactory {

  /**
   * 存储解析过的配置类
   */
  private static final Map<Class<?>, Map<String, Object>> FACTORY = new ConcurrentHashMap<>();
  /**
   * 配置属性集合
   */
  private static final Properties PROPS = new Properties();
  /**
   * 配置资源包装
   */
  private static final PropertySource SOURCE_WRAPPER = ConfigFactory::props;
  /**
   * 用于生成临时编号
   */
  private static final AtomicLong ATOMIC = new AtomicLong();
  /**
   * 临时编号前缀
   */
  private static final String TMP_PREFIX = "ConfigFactory.tmp.%s";
  /**
   * 解析过的locations
   */
  private static final Set<String> PARSED_LOCATIONS = new HashSet<>(4);

  private ConfigFactory() {
  }

  /**
   * 加载配置文件
   *
   * @param locations 路径
   */
  public static void loadProperties(String... locations) {
    loadProperties(StandardCharsets.UTF_8, true, locations);
  }

  /**
   * 加载配置文件，设置编码和忽略找不到的资源
   *
   * @param charset 字符集
   * @param ignoreNotFound 忽略未找到
   * @param locations 路径
   */
  public static void loadProperties(Charset charset, boolean ignoreNotFound, String... locations) {
    List<String> list = new LinkedList<>();
    for (String location : locations) {
      if (PARSED_LOCATIONS.add(location)) {
        list.add(location);
      }
    }
    if (list.isEmpty()) {
      return;
    }
    PropertiesLoader loader = new PropertiesLoader(list.toArray(new String[0]));
    loader.setCharset(charset);
    loader.setIgnoreNotFound(ignoreNotFound);
    loadProperties(loader);
  }

  /**
   * 加载配置文件，传入配置属性资源
   *
   * @param source 属性源
   */
  public static void loadProperties(PropertySource source) {
    PROPS.putAll(source.props());
  }

  /**
   * 获取配置属性
   *
   * @param key 属性键值
   * @return 属性值
   */
  public static String getProperty(String key) {
    return SOURCE_WRAPPER.getProperty(key);
  }

  /**
   * 获取配置属性，可设置默认值
   *
   * @param key 属性键值
   * @param defaultValue 默认值
   * @return 属性值
   */
  public static String getProperty(String key, String defaultValue) {
    return SOURCE_WRAPPER.getProperty(key, defaultValue);
  }

  /**
   * 加载配置类，需要有{@link Value}或 {@link ValueConfig}注解
   *
   * @param clazz 类
   * @param <T> 泛型类
   * @return 配置类
   */
  public static <T> T load(Class<T> clazz) {
    return load(clazz, Strings.DOT);
  }

  /**
   * 加载配置类
   *
   * @param clazz 类
   * @param prefix 前缀
   * @param <T> 泛型
   * @return 配置类
   */
  public static <T> T load(Class<T> clazz, String prefix) {
    Map<String, Object> map = FACTORY.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>(4, 1f));
    if (map.containsKey(prefix)) {
      return clazz.cast(map.get(prefix));
    }

    T val = parse(clazz, prefix);
    Object other = map.putIfAbsent(prefix, val);
    if (other != null) {
      val = clazz.cast(other);
    }
    return val;
  }

  /**
   * 加载配置类，需要有{@link Value}注解
   *
   * @param bean 对象
   */
  public static void load(Object bean) {
    if (bean == null) {
      return;
    }
    ValueConfig config = ClassUtils
        .getAnnotation(ClassUtils.getCglibActualClass(bean.getClass()), ValueConfig.class);
    if (config == null) {
      load(bean, null);
    } else {
      load(bean, config.value());
    }
  }

  /**
   * 加载配置类，需要有{@link Value}注解
   *
   * @param bean 对象
   * @param prefix 前缀
   */
  public static void load(Object bean, String prefix) {
    if (bean != null) {
      parse(bean, prefix);
    }
  }

  /**
   * 清除配置
   */
  public static void clear() {
    FACTORY.clear();
    PROPS.clear();
    PARSED_LOCATIONS.clear();
  }

  /**
   * 获取所有属性名称
   *
   * @return keys
   */
  public static Set<String> keys() {
    return PROPS.stringPropertyNames();
  }

  /**
   * 解析
   *
   * @param clazz 类
   * @param prefix 前綴
   * @param <T> 泛型类
   * @return 配置类
   */
  protected static <T> T parse(Class<T> clazz, String prefix) {
    T obj;
    try {
      obj = clazz.getConstructor().newInstance();
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
    Class<?> actualClass = ClassUtils.getCglibActualClass(clazz);
    if (Strings.DOT.equals(prefix) && actualClass.isAnnotationPresent(ValueConfig.class)) {
      prefix = actualClass.getAnnotation(ValueConfig.class).value();
    }
    return clazz.cast(parse(obj, prefix));
  }

  protected static Object parse(Object obj, String prefix) {
    Class<?> clazz = obj.getClass();
    Field[] fields = ClassUtils.getAllDeclaredFields(clazz);
    for (Field field : fields) {
      if (field.isAnnotationPresent(Value.class)) {
        Value val = field.getAnnotation(Value.class);
        Object value = getProperty(val.value(), field.getType());
        if (value != null) {
          ClassUtils.setValue(obj, field, value);
        }
      } else if (prefix != null) {
        StringBuilder name = new StringBuilder(prefix);
        if (name.length() > 0) {
          name.append(Strings.DOT);
        }
        name.append(field.getName());
        Object value = getProperty(name.toString(), field.getType(), false);
        if (value != null) {
          ClassUtils.setValue(obj, field, value);
        }
      }
    }
    return obj;
  }

  private static Object getProperty(String key, Class<?> type) {
    return getProperty(key, type, true);
  }

  private static Object getProperty(String key, Class<?> type, boolean wrap) {
    String value;
    if (wrap) {
      String tmpKey = String.format(TMP_PREFIX, ATOMIC.getAndIncrement());
      PROPS.setProperty(tmpKey, key);
      value = getProperty(tmpKey);
      PROPS.remove(tmpKey);
    } else {
      value = getProperty(key);
    }
    if (value == null || value.getClass() == type) {
      return value;
    }
    return DefaultConverterService.sharedConverterService().convert(value, type);
  }

  private static Properties props() {
    Properties properties = new Properties(PROPS);
    properties.putAll(System.getProperties());
    return properties;
  }
}
