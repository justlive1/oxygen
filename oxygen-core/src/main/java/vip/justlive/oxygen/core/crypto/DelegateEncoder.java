package vip.justlive.oxygen.core.crypto;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理加密
 *
 * @author wubo
 */
public class DelegateEncoder extends BaseEncoder {

  private static final String ERROR_TEMPLATE = "There is no encoder mapped for the id \"%s\"";

  private static final Map<String, Encoder> ENCODERS = new ConcurrentHashMap<>();

  static {
    ENCODERS.put("NoOp", new NoOpEncoder());
    ENCODERS.put("MD5", new Md5Encoder());
  }

  private final String id;

  public DelegateEncoder(String id) {
    if (!ENCODERS.containsKey(id)) {
      throw new IllegalArgumentException(String.format(ERROR_TEMPLATE, id));
    }
    this.id = id;
  }

  @Override
  public String encode(String source) {
    String salt = PREFIX.concat(id).concat(SUFFIX);
    return salt.concat(doEncode(source));
  }

  @Override
  String doEncode(String source) {
    Encoder encoder = ENCODERS.get(id);
    return encoder.encode(source);
  }

  @Override
  public boolean match(String source, String raw) {
    String salt = extractSalt(raw);
    Encoder encoder = ENCODERS.get(salt);
    if (encoder == null) {
      throw new IllegalArgumentException(String.format(ERROR_TEMPLATE, salt));
    }
    String value = PREFIX.concat(salt).concat(SUFFIX).concat(encoder.encode(source));
    return Objects.equals(value, raw);
  }

  @Override
  public void setUseSalt(boolean useSalt) {
    throw new UnsupportedOperationException();
  }
}
