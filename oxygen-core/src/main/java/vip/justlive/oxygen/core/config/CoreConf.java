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

import lombok.Data;

/**
 * 系统配置类
 *
 * @author wubo
 */
@Data
public class CoreConf {

  /**
   * 临时文件base目录
   */
  @Value("${main.temp.dir:.oxygen}")
  private String baseTempDir;
  /**
   * 类扫描路径属性
   */
  @Value("${main.class.scan:}")
  private String classScan;

  /**
   * 缓存实现类
   */
  @Value("${cache.impl.class:}")
  private String cacheImplClass;

  /**
   * override配置文件地址属性
   */
  @Value("${config.override.path:}")
  private String configOverridePath;

  /**
   * job线程名称格式
   */
  @Value("${job.thread.name.format:jobs-%d}")
  private String jobThreadFormat;

  /**
   * job核心线程池大小
   */
  @Value("${job.core.pool.size:10}")
  private Integer jobPoolSize;

  /**
   * i18n配置文件地址
   */
  @Value("${i18n.path:classpath:message/*.properties}")
  private String[] i18nPath;

  /**
   * i18n默认语言
   */
  @Value("${i18n.default.language:zh}")
  private String i18nDefaultLanguage;

  /**
   * i18n默认国家
   */
  @Value("${i18n.default.language:CN}")
  private String i18nDefaultCountry;

  /**
   * i18n参数key
   */
  @Value("${i18n.param.key:locale}")
  private String i18nParamKey;

  /**
   * i18n Session key
   */
  @Value("${i18n.session.key:I18N_SESSION_LOCALE}")
  private String i18nSessionKey;
}
