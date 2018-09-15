package vip.justlive.oxygen.core;

import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * 引导类
 * <br>
 * 用于加载程序启动需要的配置
 *
 * @author wubo
 */
public final class Bootstrap {

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
}
