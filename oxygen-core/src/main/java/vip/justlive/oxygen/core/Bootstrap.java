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
package vip.justlive.oxygen.core;

import java.util.Arrays;
import vip.justlive.oxygen.core.aop.AopPlugin;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.ioc.IocPlugin;
import vip.justlive.oxygen.core.job.JobPlugin;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;

/**
 * 引导类
 * <br>
 * 用于加载程序启动需要的配置
 *
 * @author wubo
 */
public final class Bootstrap {

  Bootstrap() {
  }

  /**
   * 初始化配置
   * <br>
   * 使用默认地址进行加载，然后使用覆盖路径再次加载
   */
  public static void initConfig() {
    initConfig(Constants.CONFIG_PATHS);
  }

  /**
   * 初始化配置
   *
   * @param locations 配置文件路径
   */
  public static void initConfig(String... locations) {
    ConfigFactory.loadProperties(locations);
    String overridePath = ConfigFactory.getProperty(Constants.CONFIG_OVERRIDE_PATH_KEY);
    if (overridePath != null) {
      ConfigFactory.loadProperties(overridePath.split(Constants.COMMA));
    }
  }

  /**
   * 初始化系统插件类
   */
  public static void initSystemPlugin() {
    Plugin[] plugins = new Plugin[]{new ClassScannerPlugin(), new IocPlugin(), new AopPlugin(),
        new JobPlugin()};
    Arrays.sort(plugins);
    for (Plugin plugin : plugins) {
      plugin.start();
    }
  }

}
