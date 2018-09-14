package vip.justlive.oxygen.core.crypto;

/**
 * 加密接口
 *
 * @author wubo
 */
public interface Encoder {

  /**
   * 加密
   *
   * @param source 源
   * @return 加密字符串
   */
  String encode(String source);

  /**
   * 是否匹配
   *
   * @param source 源
   * @param raw 加密后数据
   * @return true表示匹配
   */
  boolean match(String source, String raw);
}
