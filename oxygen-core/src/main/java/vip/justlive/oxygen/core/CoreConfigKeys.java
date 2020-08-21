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
package vip.justlive.oxygen.core;

import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.config.ConfigKey;

/**
 * core config keys
 *
 * @author wubo
 */
@UtilityClass
public class CoreConfigKeys {

  public ConfigKey CACHE_CLASS = new ConfigKey("oxygen.cache.class");

  public ConfigKey CLASS_SCAN = new ConfigKey("oxygen.class.scan");

  public ConfigKey AOP_ENABLED = new ConfigKey("oxygen.aop.enabled", "true");

  public ConfigKey COMPILER_OPTIONS = new ConfigKey("oxygen.compiler.options");

  public ConfigKey CONFIG_OVERRIDE_PATH = new ConfigKey("oxygen.config.override.path");

  public ConfigKey JOB_THREAD_NAME_FORMAT = new ConfigKey("oxygen.job.threadNameFormat", "jobs-%d");
  public ConfigKey JOB_CORE_POOL_SIZE = new ConfigKey("oxygen.job.corePoolSize", "10");

  public ConfigKey I18N_PATH = new ConfigKey("oxygen.i18n.path", "classpath:message/*.properties");
  public ConfigKey I18N_LANGUAGE = new ConfigKey("oxygen.i18n.language", "zh");
  public ConfigKey I18N_COUNTRY = new ConfigKey("oxygen.i18n.country", "CN");
  public ConfigKey I18N_PARAM_KEY = new ConfigKey("oxygen.i18n.param.key", "locale");
  public ConfigKey I18N_SESSION_KEY = new ConfigKey("oxygen.i18n.session.key",
      "I18N_SESSION_LOCALE");

  public ConfigKey TEMP_DIR = new ConfigKey("oxygen.temp.dir");

  public ConfigKey THREAD_POOL_SIZE = new ConfigKey("oxygen.threadPool.size", "10");
  public ConfigKey THREAD_POOL_QUEUE = new ConfigKey("oxygen.threadPool.queue", "100000");

  public ConfigKey WHEEL_TIMER_DURATION = new ConfigKey("oxygen.wheelTimer.duration", "1");
  public ConfigKey WHEEL_TIMER_WHEEL_SIZE = new ConfigKey("oxygen.wheelTimer.wheelSize", "60");
  public ConfigKey WHEEL_TIMER_POOL_SIZE = new ConfigKey("oxygen.wheelTimer.poolSize", "10");
  public ConfigKey WHEEL_TIMER_TIMEOUT = new ConfigKey("oxygen.wheelTimer.timeout", "100");

  public ConfigKey AIO_USE_FUTURE = new ConfigKey("oxygen.aio.future", "false");
}
