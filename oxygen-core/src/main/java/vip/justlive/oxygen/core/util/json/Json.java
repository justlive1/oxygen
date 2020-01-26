package vip.justlive.oxygen.core.util.json;

import lombok.experimental.UtilityClass;

/**
 * json
 *
 * @author wubo
 */
@UtilityClass
public class Json {

  private static final JsonWriter WRITER = new JsonWriter();

  /**
   * object to string
   *
   * @param value object
   * @return string
   */
  public static String toJson(Object value) {
    return WRITER.toJson(value);
  }

}
