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
package vip.justlive.oxygen.core.i18n;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.io.AbstractResourceLoader;
import vip.justlive.oxygen.core.io.SourceResource;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * i18n
 *
 * @author wubo
 */
@Slf4j
public class Lang extends AbstractResourceLoader implements Plugin {

  private static final Properties PROPS = new Properties();
  private static final Map<String, Properties> I18N = new HashMap<>(2);
  private static final ThreadLocal<Locale> LOCALE = ThreadLocal.withInitial(Lang::defaultLocale);

  public Lang() {
    init();
  }

  @Override
  public void init() {
    this.loader = ClassUtils.getDefaultClassLoader();
    this.ignoreNotFound = true;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 5;
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
      String[] arr = resource.path().split(Constants.UNDERSCORE);
      try {
        if (arr.length == 1) {
          PROPS.load(getReader(resource));
        } else if (arr.length == 3) {
          I18N.computeIfAbsent(
              arr[1] + Constants.UNDERSCORE + arr[2].substring(0, arr[2].indexOf(Constants.DOT)),
              k -> new Properties()).load(getReader(resource));
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
    Properties props = I18N.get(language + Constants.UNDERSCORE + country);
    String value = null;
    if (props != null) {
      value = props.getProperty(key);
    }
    if (value == null) {
      value = PROPS.getProperty(key);
    }
    return value;
  }

  @Override
  public void start() {
    CoreConf conf = ConfigFactory.load(CoreConf.class);
    load(conf.getI18nPath());
  }

  @Override
  public void stop() {
    PROPS.clear();
    I18N.clear();
  }

  private static Locale defaultLocale() {
    CoreConf conf = ConfigFactory.load(CoreConf.class);
    if (conf.getI18nDefaultLanguage() != null && conf.getI18nDefaultLanguage().length() > 0
        && conf.getI18nDefaultCountry() != null && conf.getI18nDefaultCountry().length() > 0) {
      return new Locale(conf.getI18nDefaultLanguage(), conf.getI18nDefaultCountry());
    }
    return Locale.getDefault();
  }
}
