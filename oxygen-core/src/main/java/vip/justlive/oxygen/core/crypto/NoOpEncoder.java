package vip.justlive.oxygen.core.crypto;

/**
 * 不加密
 *
 * @author wubo
 */
public class NoOpEncoder extends BaseEncoder {

  @Override
  String doEncode(String source) {
    return source;
  }

}
