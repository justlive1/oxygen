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
package vip.justlive.oxygen.core.config;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.convert.DefaultConverterService;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.io.PropertiesLoader;
import vip.justlive.oxygen.core.io.PropertySource;
import vip.justlive.oxygen.core.util.ReflectUtils;

/**
 * 配置工厂
 *
 * @author wubo
 */
@Slf4j
public class ConfigFactory {

  /**
   * 存储解析过的配置类
   */
  private static final Map<Class<?>, Object> FACTORY = new ConcurrentHashMap<>();
  /**
   * 配置属性集合
   */
  private static final Properties PROPS = new Properties();
  /**
   * 配置资源包装
   */
  private static final PropertySource SOURCE_WRAPPER = () -> PROPS;
  /**
   * 用于生成临时编号
   */
  private static final AtomicLong ATOMIC = new AtomicLong();
  /**
   * 临时编号前缀
   */
  private static final String TMP_PREFIX = "ConfigFactory.tmp.%s";

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
    PropertiesLoader loader = new PropertiesLoader(locations);
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
   * 加载配置类，需要有{@link Value}注解
   *
   * @param clazz 类
   * @param <T> 泛型类
   * @return 配置类
   */
  public static <T> T load(Class<T> clazz) {
    Object obj = FACTORY.get(clazz);
    if (obj != null) {
      return clazz.cast(obj);
    }

    T val = parse(clazz);
    Object other = FACTORY.putIfAbsent(clazz, val);
    if (other != null) {
      val = clazz.cast(other);
    }
    return val;
  }

  /**
   * 解析
   *
   * @param clazz 类
   * @param <T> 泛型类
   * @return 配置类
   */
  protected static <T> T parse(Class<T> clazz) {
    Field[] fields = ReflectUtils.getAllDeclaredFields(clazz);
    T obj;
    try {
      obj = clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw Exceptions.wrap(e);
    }
    for (Field field : fields) {
      if (field.isAnnotationPresent(Value.class)) {
        Value val = field.getAnnotation(Value.class);
        Object value = getProperty(val.value(), field.getType());
        if (value != null) {
          field.setAccessible(true);
          try {
            field.set(obj, value);
          } catch (IllegalArgumentException | IllegalAccessException e) {
            log.error("set value {} to class {} error", value, clazz, e);
          }
        }
      }
    }
    return obj;
  }

  private static Object getProperty(String key, Class<?> type) {
    String tmpKey = String.format(TMP_PREFIX, ATOMIC.getAndIncrement());
    PROPS.setProperty(tmpKey, key);
    String value = getProperty(tmpKey);
    PROPS.remove(tmpKey);
    if (value.getClass() == type) {
      return value;
    }
    return DefaultConverterService.sharedConverterService().convert(value, type);
  }

}
