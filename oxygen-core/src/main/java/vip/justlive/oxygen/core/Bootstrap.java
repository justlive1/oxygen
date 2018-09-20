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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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

  private static final List<Plugin> PLUGINS = new ArrayList<>(5);
  private static final AtomicBoolean STATE = new AtomicBoolean(false);
  private static final Thread SHUTDOWN_HOOK = new Thread(Bootstrap::doClose);

  Bootstrap() {
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
   * 添加自定义插件
   *
   * @param plugins 插件
   */
  public static void addCustomPlugin(Plugin... plugins) {
    if (STATE.get()) {
      throw new IllegalStateException("Bootstrap已启动");
    }
    PLUGINS.addAll(Arrays.asList(plugins));
  }

  /**
   * 启动Bootstrap
   */
  public static void start() {
    if (STATE.compareAndSet(false, true)) {
      initConfig();
      addSystemPlugin();
      initPlugins();
      registerShutdownHook();
    }
  }

  /**
   * 关闭Bootstrap
   */
  public synchronized static void close() {
    doClose();
    Runtime.getRuntime().removeShutdownHook(SHUTDOWN_HOOK);
  }

  /**
   * 初始化配置
   * <br>
   * 使用默认地址进行加载，然后使用覆盖路径再次加载
   */
  private static void initConfig() {
    initConfig(Constants.CONFIG_PATHS);
  }

  /**
   * 添加系统插件类
   */
  private static void addSystemPlugin() {
    PLUGINS.add(new ClassScannerPlugin());
    PLUGINS.add(new IocPlugin());
    PLUGINS.add(new AopPlugin());
    PLUGINS.add(new JobPlugin());
  }

  /**
   * 初始化插件
   */
  private static void initPlugins() {
    Collections.sort(PLUGINS);
    for (Plugin plugin : PLUGINS) {
      plugin.start();
    }
  }

  /**
   * 注册shutdown钩子
   */
  private synchronized static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
  }

  /**
   * 关闭Bootstrap
   */
  private static void doClose() {
    for (Plugin plugin : PLUGINS) {
      plugin.stop();
    }
  }
}
