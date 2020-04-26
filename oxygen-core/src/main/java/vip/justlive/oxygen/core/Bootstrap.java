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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.WrappedException;
import vip.justlive.oxygen.core.util.FileUtils;
import vip.justlive.oxygen.core.util.IoUtils;
import vip.justlive.oxygen.core.util.ServiceLoaderUtils;
import vip.justlive.oxygen.core.util.Strings;
import vip.justlive.oxygen.core.util.ThreadUtils;

/**
 * 引导类
 * <br>
 * 用于加载程序启动需要的配置
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class Bootstrap {

  private final List<Plugin> PLUGINS = new ArrayList<>(8);
  private final AtomicBoolean STATE = new AtomicBoolean(false);
  private final Thread SHUTDOWN_HOOK;
  private final String VERSION;

  static {
    String version;
    try (InputStream in = Bootstrap.class
        .getResourceAsStream("/vip/justlive/oxygen/core/Version")) {
      version = IoUtils.toString(in);
    } catch (Exception e) {
      version = "oxygen/unknown";
    }
    VERSION = version;
    SHUTDOWN_HOOK = ThreadUtils.defaultThreadFactory().newThread(Bootstrap::doClose);
  }

  /**
   * 版本
   *
   * @return version
   */
  public String version() {
    return VERSION;
  }

  /**
   * 初始化配置
   * <br>
   * 使用默认地址进行加载，然后使用覆盖路径再次加载
   */
  public void initConfig() {
    initConfig("classpath*:config.properties", "classpath*:/config/*.properties");
  }

  /**
   * 初始化配置
   *
   * @param locations 配置文件路径
   */
  public void initConfig(String... locations) {
    ConfigFactory.loadProperties(locations);
    String overridePath = ConfigFactory.getProperty("config.override.path");
    if (overridePath != null && overridePath.length() > 0) {
      ConfigFactory.loadProperties(overridePath.split(Strings.COMMA));
    }
  }

  /**
   * 添加自定义插件
   *
   * @param plugins 插件
   */
  public void addCustomPlugin(Plugin... plugins) {
    if (STATE.get()) {
      throw new IllegalStateException("Bootstrap已启动");
    }
    PLUGINS.addAll(Arrays.asList(plugins));
  }

  /**
   * 获取启动的插件
   *
   * @return plugins
   */
  public List<Plugin> enabledPlugins() {
    return Collections.unmodifiableList(PLUGINS);
  }

  /**
   * 注册默认线程异常处理
   */
  public void registerUncaughtExceptionHandler() {
    if (Thread.getDefaultUncaughtExceptionHandler() == null) {
      Thread.setDefaultUncaughtExceptionHandler(Bootstrap::handleException);
    }
  }

  /**
   * 启动Bootstrap
   */
  public void start() {
    if (STATE.compareAndSet(false, true)) {
      log.info("starting bootstrap ...");
      registerUncaughtExceptionHandler();
      initConfig();
      addSystemPlugin();
      initPlugins();
      registerShutdownHook();
      log.info("bootstrap has started ! have fun");
    }
  }

  /**
   * 关闭Bootstrap
   */
  public synchronized void close() {
    doClose();
    Runtime.getRuntime().removeShutdownHook(SHUTDOWN_HOOK);
  }

  /**
   * 等待容器关闭，一般在没有web容器时使用
   */
  public void sync() {
    if (!STATE.get() || Thread.currentThread().isInterrupted()) {
      return;
    }

    synchronized (STATE) {
      while (STATE.get()) {
        try {
          STATE.wait();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }

  /**
   * 添加系统插件类
   */
  private void addSystemPlugin() {
    PLUGINS.addAll(ServiceLoaderUtils.loadServices(Plugin.class));
  }

  /**
   * 初始化插件
   */
  private void initPlugins() {
    Collections.sort(PLUGINS);
    PLUGINS.forEach(Plugin::start);
  }

  /**
   * 注册shutdown钩子
   */
  private synchronized void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
  }

  /**
   * 关闭Bootstrap
   */
  private void doClose() {
    log.info("closing bootstrap ...");
    PLUGINS.forEach(Plugin::stop);
    FileUtils.cleanTempBaseDir();
    STATE.set(false);
    synchronized (STATE) {
      STATE.notifyAll();
    }
    log.info("bootstrap closed ! bye bye");
  }

  private void handleException(Thread thread, Throwable throwable) {
    if (throwable instanceof WrappedException) {
      throwable = ((WrappedException) throwable).getException();
    }
    log.error("thread [{}] uncaught exception", thread.getName(), throwable);
  }
}
