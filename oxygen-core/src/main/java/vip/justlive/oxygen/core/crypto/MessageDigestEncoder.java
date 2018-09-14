package vip.justlive.oxygen.core.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import vip.justlive.oxygen.core.util.Hex;

/**
 * MessageDigest实现的encoder
 *
 * @author wubo
 */
public class MessageDigestEncoder extends BaseEncoder {

  private final String algorithm;

  public MessageDigestEncoder(String algorithm) {
    this.algorithm = algorithm;
  }

  @Override
  String doEncode(String source) {
    byte[] bytes = create().digest(StandardCharsets.UTF_8.encode(source).array());
    return Hex.encodeToString(bytes);
  }

  private MessageDigest create() {
    try {
      return MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("No such hashing algorithm", e);
    }
  }

}
