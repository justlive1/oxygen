package vip.justlive.oxygen.core.crypto;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * 基础加密类
 *
 * @author wubo
 */
@Getter
@Setter
public abstract class BaseEncoder implements Encoder {

  public static final String PREFIX = "{";
  public static final String SUFFIX = "}";
  public static final String EMPTY = "";

  protected int iterations = 1;
  protected boolean useSalt = false;
  protected int saltKeyLength = 8;

  private final SecureRandom random = new SecureRandom();

  @Override
  public String encode(String source) {
    String salt = EMPTY;
    if (useSalt) {
      salt = generateSaltKey();
    }
    return encode(source, salt);
  }

  protected String encode(String source, String salt) {
    String value = source.concat(salt);
    for (int i = 0; i < iterations; i++) {
      value = doEncode(value);
    }
    if (!EMPTY.equals(salt)) {
      value = PREFIX.concat(salt).concat(SUFFIX).concat(value);
    }
    return value;
  }

  @Override
  public boolean match(String source, String raw) {
    String salt = extractSalt(raw);
    String value = encode(source, salt);
    return Objects.equals(value, raw);
  }

  /**
   * 执行加密
   *
   * @param source 源数据
   * @return 加密字符串
   */
  abstract String doEncode(String source);

  /**
   * 生成salt
   *
   * @return salt
   */
  public String generateSaltKey() {
    byte[] bytes = new byte[saltKeyLength];
    random.nextBytes(bytes);
    return Base64.getEncoder().encodeToString(bytes);
  }

  /**
   * 生成并包装salt
   *
   * @param salt salt
   * @return
   */
  public String wrapperSalt(String salt) {
    return PREFIX.concat(salt).concat(SUFFIX);
  }

  /**
   * 去除包装
   *
   * @param raw 包装后的salt
   * @return salt
   */
  public String extractSalt(String raw) {
    int start = raw.indexOf(PREFIX);
    if (start != 0) {
      return EMPTY;
    }
    int end = raw.indexOf(SUFFIX, start);
    if (end < 0) {
      return EMPTY;
    }
    return raw.substring(start + 1, end);
  }
}
