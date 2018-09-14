package vip.justlive.oxygen.core.io;

import java.util.Properties;
import vip.justlive.oxygen.core.util.PlaceHolderHelper;

/**
 * 配置文件源
 *
 * @author wubo
 */
@FunctionalInterface
public interface PropertySource {

  /**
   * 获取属性集合
   *
   * @return 属性集合
   */
  Properties props();

  /**
   * 获取属性
   *
   * @param key 属性键值
   * @return 属性值
   */
  default String getProperty(String key) {
    String value = props().getProperty(key);
    if (value == null) {
      return value;
    }
    return PlaceHolderHelper.DEFAULT_HELPER.replacePlaceholders(value, props());
  }

  /**
   * 获取属性，可设置默认值
   *
   * @param key 属性键值
   * @param defaultValue 默认值
   * @return 属性值
   */
  default String getProperty(String key, String defaultValue) {
    String value = props().getProperty(key, defaultValue);
    if (value == null) {
      return value;
    }
    return PlaceHolderHelper.DEFAULT_HELPER.replacePlaceholders(value, props());
  }
}
