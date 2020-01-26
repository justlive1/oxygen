package vip.justlive.oxygen.core.util.json.codec;

/**
 * String 序列化
 *
 * @author wubo
 */
public class StringSerializer implements Serializer {

  @Override
  public boolean supported(Class<?> type, Object value) {
    return CharSequence.class.isAssignableFrom(type);
  }

  @Override
  public void serialize(Object value, StringBuilder buf) {
    String string = value.toString();
    buf.append('"');
    for (int i = 0, len = string.length(); i < len; i++) {
      char c = string.charAt(i);
      switch (c) {
        case '\b':
          buf.append("\\b");
          break;
        case '\t':
          buf.append("\\t");
          break;
        case '\n':
          buf.append("\\n");
          break;
        case '\f':
          buf.append("\\f");
          break;
        case '\r':
          buf.append("\\r");
          break;
        case '"':
        case '\\':
          buf.append('\\').append(c);
          break;
        default:
          if (c < ' ') {
            String t = "000" + Integer.toHexString(c);
            buf.append("\\u").append(t, 0, t.length() - 4);
          } else {
            buf.append(c);
          }
      }
    }
    buf.append('"');
  }
}
