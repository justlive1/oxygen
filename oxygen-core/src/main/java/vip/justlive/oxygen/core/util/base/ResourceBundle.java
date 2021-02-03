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

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.CoreConfigKeys;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.io.AbstractResourceLoader;
import vip.justlive.oxygen.core.util.io.SourceResource;

/**
 * i18n
 *
 * @author wubo
 */
@Slf4j
public class ResourceBundle extends AbstractResourceLoader implements Plugin {

  private static final Properties PROPS = new Properties();
  private static final Map<String, Properties> I18N = new HashMap<>(2);
  private static final ThreadLocal<Locale> LOCALE = ThreadLocal
      .withInitial(ResourceBundle::defaultLocale);

  public ResourceBundle() {
    init();
  }

  /**
   * 当前线程区域
   *
   * @return 区域
   */
  public static Locale currentThreadLocale() {
    return LOCALE.get();
  }

  /**
   * 设置当前线程区域
   *
   * @param locale 区域
   */
  public static void setThreadLocale(Locale locale) {
    LOCALE.set(locale);
  }

  /**
   * 清除当前线程区域设置
   */
  public static void clearThreadLocale() {
    LOCALE.remove();
  }

  /**
   * 根据key获取message
   *
   * @param key key
   * @return message
   */
  public static String getMessage(String key) {
    Locale locale = currentThreadLocale();
    if (locale == null) {
      locale = Locale.getDefault();
    }
    return getMessage(key, locale);
  }

  /**
   * 根据key获取message
   *
   * @param key key
   * @param locale 区域
   * @return message
   */
  public static String getMessage(String key, Locale locale) {
    return getMessage(key, locale.getLanguage(), locale.getCountry());
  }

  /**
   * 根据key获取message
   *
   * @param key key
   * @param language 语言
   * @param country 国家
   * @return message
   */
  public static String getMessage(String key, String language, String country) {
    Properties props = I18N.get(language + Strings.UNDERSCORE + country);
    String value = null;
    if (props != null) {
      value = props.getProperty(key);
    }
    if (value == null) {
      value = PROPS.getProperty(key);
    }
    return value;
  }

  private static Locale defaultLocale() {
    String language = CoreConfigKeys.I18N_LANGUAGE.getValue();
    String country = CoreConfigKeys.I18N_COUNTRY.getValue();
    if (Strings.hasText(language) && Strings.hasText(country)) {
      return new Locale(language, country);
    }
    return Locale.getDefault();
  }

  @Override
  public void init() {
    this.loader = ClassUtils.getDefaultClassLoader();
    this.ignoreNotFound = true;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 200;
  }

  /**
   * 加载i18n文件
   *
   * @param locations 路径
   */
  public void load(String... locations) {
    List<SourceResource> res = this.parse(locations);
    for (SourceResource resource : res) {
      this.resources.add(resource);
      String[] arr = resource.path().split(Strings.UNDERSCORE);
      try (Reader reader = getReader(resource)) {
        if (arr.length == 1) {
          PROPS.load(reader);
        } else if (arr.length == 3) {
          I18N.computeIfAbsent(
              arr[1] + Strings.UNDERSCORE + arr[2].substring(0, arr[2].indexOf(Strings.DOT)),
              k -> new Properties()).load(reader);
        } else {
          log.warn("file [{}] used an illegal name", resource.path());
        }
      } catch (IOException e) {
        if (ignoreNotFound) {
          log.warn("i18n file cannot find [{}]", resource.path());
        } else {
          throw Exceptions.wrap(e);
        }
      }
    }
  }

  @Override
  public void start() {
    load(CoreConfigKeys.I18N_PATH.getValue());
  }

  @Override
  public void stop() {
    PROPS.clear();
    I18N.clear();
  }
}
