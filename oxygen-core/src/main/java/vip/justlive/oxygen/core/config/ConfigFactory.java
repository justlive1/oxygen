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
package vip.justlive.oxygen.core.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.io.MultiPropertySource;
import vip.justlive.oxygen.core.util.io.PropertiesLoader;
import vip.justlive.oxygen.core.util.io.PropertiesPropertySource;
import vip.justlive.oxygen.core.util.io.PropertySource;
import vip.justlive.oxygen.core.util.io.SystemEnvPropertySource;
import vip.justlive.oxygen.core.util.io.SystemPropertySource;

/**
 * 配置工厂
 *
 * @author wubo
 */
@UtilityClass
public class ConfigFactory {

  private final Map<Class<?>, Map<String, Object>> FACTORY = new ConcurrentHashMap<>();
  private final Set<String> PARSED_LOCATIONS = new HashSet<>(4);
  private final AtomicInteger INDEX = new AtomicInteger(0);

  private final PropertiesPropertySource APP_SOURCE;
  private final MultiPropertySource SOURCE;
  private final Binder BINDER;

  static {
    APP_SOURCE = new PropertiesPropertySource().setOrder(Integer.MIN_VALUE);
    SOURCE = new MultiPropertySource().addSource(APP_SOURCE)
        .addSource(new SystemEnvPropertySource()).addSource(new SystemPropertySource());
    BINDER = new Binder(SOURCE);
  }

  /**
   * 加载配置文件
   *
   * @param locations 路径
   */
  public void loadProperties(String... locations) {
    loadProperties(StandardCharsets.UTF_8, true, locations);
  }

  /**
   * 加载配置文件
   *
   * @param order 优先级
   * @param locations 路径
   */
  public void loadProperties(int order, String... locations) {
    loadProperties(order, StandardCharsets.UTF_8, true, locations);
  }

  /**
   * 加载配置文件，设置编码和忽略找不到的资源
   *
   * @param charset 字符集
   * @param ignoreNotFound 忽略未找到
   * @param locations 路径
   */
  public void loadProperties(Charset charset, boolean ignoreNotFound, String... locations) {
    loadProperties(INDEX.getAndDecrement(), charset, ignoreNotFound, locations);
  }

  /**
   * 加载配置文件，设置编码和忽略找不到的资源
   *
   * @param order 优先级
   * @param charset 字符集
   * @param ignoreNotFound 忽略未找到
   * @param locations 路径
   */
  public void loadProperties(int order, Charset charset, boolean ignoreNotFound,
      String... locations) {
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
    loader.setOrder(order);
    loadProperties(loader);
  }

  /**
   * 加载配置文件，传入配置属性资源
   *
   * @param source 属性源
   */
  public void loadProperties(PropertySource source) {
    SOURCE.addSource(source);
    FACTORY.clear();
  }

  /**
   * 设置配置属性
   *
   * @param key 键
   * @param value 值
   */
  public void setProperty(String key, String value) {
    MoreObjects.notNull(key, "key can not be null");
    APP_SOURCE.setProperty(key, value);
    FACTORY.clear();
  }

  /**
   * 获取配置属性
   *
   * @param key 属性键值
   * @return 属性值
   */
  public String getProperty(String key) {
    return SOURCE.getProperty(key);
  }

  /**
   * 获取占位符属性
   *
   * @param placeholder 占位 eg. ${a}
   * @return 属性值
   */
  public String getPlaceholderProperty(String placeholder) {
    return SOURCE.getPlaceholderProperty(placeholder);
  }

  /**
   * 获取配置属性，可设置默认值
   *
   * @param key 属性键值
   * @param defaultValue 默认值
   * @return 属性值
   */
  public String getProperty(String key, String defaultValue) {
    return SOURCE.getProperty(key, defaultValue);
  }

  /**
   * 加载配置类，需要有{@link Value}或 {@link ValueConfig}注解
   *
   * @param clazz 类
   * @param <T> 泛型类
   * @return 配置类
   */
  public <T> T load(Class<T> clazz) {
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
  public <T> T load(Class<T> clazz, String prefix) {
    Map<String, Object> map = FACTORY.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>(4, 1f));
    if (map.containsKey(prefix)) {
      return clazz.cast(map.get(prefix));
    }

    T val = BINDER.bind(clazz, prefix);
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
  public void load(Object bean) {
    if (bean == null) {
      return;
    }
    ValueConfig config = ClassUtils
        .getAnnotation(ClassUtils.getActualClass(bean.getClass()), ValueConfig.class);
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
  public void load(Object bean, String prefix) {
    if (bean != null) {
      BINDER.bind(bean, prefix);
    }
  }

  /**
   * 清除配置
   */
  public void clear() {
    FACTORY.clear();
    APP_SOURCE.clear();
    SOURCE.clear().addSource(APP_SOURCE).addSource(new SystemEnvPropertySource())
        .addSource(new SystemPropertySource());
    PARSED_LOCATIONS.clear();
  }

  /**
   * 获取所有属性名称
   *
   * @return keys
   */
  public Set<String> keys() {
    return SOURCE.props().stringPropertyNames();
  }

}
