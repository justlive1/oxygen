package vip.justlive.oxygen.core.util;

import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * string utils
 *
 * @author wubo
 * @since 2.1.2
 */
@UtilityClass
public class Strings {

  public static final String AND = "&";
  public static final String ANY = "*";
  public static final String ANY_PATH = "/*";
  public static final String CLOSE_BRACE = "}";
  public static final String CLOSE_BRACKET = "]";
  public static final String CLOSE_PAREN = ")";
  public static final String COLON = ":";
  public static final String COMMA = ",";
  public static final String DASH = "-";
  public static final String DOLLAR = "$";
  public static final String DOT = ".";
  public static final String DOUBLE_DOLLAR = "$$";
  public static final String DOUBLE_QUOTATION_MARK = "\"";
  public static final String EMPTY = "";
  public static final String EQUAL = "=";
  public static final String OCTOTHORP = "#";
  public static final String OPEN_BRACE = "{";
  public static final String OPEN_BRACKET = "[";
  public static final String OPEN_PAREN = "(";
  public static final String PLUS = "+";
  public static final String QUESTION_MARK = "?";
  public static final String SEMICOLON = ";";
  public static final String SLASH = "/";
  public static final String UNDERSCORE = "_";
  public static final String UNKNOWN = "unknown";
  public static final String FILE_PREFIX = "file:";
  public static final String ALL_CLASSPATH_PREFIX = "classpath*:";
  public static final String CLASSPATH_PREFIX = "classpath:";
  public static final String[] EMPTY_ARRAY = new String[0];

  /**
   * 字符串是否有值
   *
   * @param text 字符串
   * @return true为有值
   */
  public static boolean hasText(String text) {
    return text != null && text.trim().length() > 0;
  }

  /**
   * 获取第一个不为null或空字符串的值
   *
   * @param first first value
   * @param second second value
   * @param others other values
   * @return nonEmpty
   */
  public static String firstNonNull(String first, String second, String... others) {
    String str = firstOrNull(first, second, others);
    if (str != null) {
      return str;
    }
    throw Exceptions.fail("no text have non-empty value");
  }


  /**
   * 获取第一个不为null或空字符串的值
   *
   * @param first first value
   * @param second second value
   * @param others other values
   * @return nonEmpty
   */
  public static String firstOrNull(String first, String second, String... others) {
    if (hasText(first)) {
      return first;
    }
    if (hasText(second)) {
      return second;
    }
    if (others != null) {
      for (String str : others) {
        if (hasText(str)) {
          return str;
        }
      }
    }
    return null;
  }
}
