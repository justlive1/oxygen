package vip.justlive.oxygen.core.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * placeHolder帮助类
 * <br>
 * 用于解析value中使用占位的情况
 *
 * @author wubo
 */
public class PlaceHolderHelper {

  /**
   * 默认占位符前缀: {@value}
   */
  public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

  /**
   * 默认占位符后缀: {@value}
   */
  public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

  /**
   * 默认占位值分隔符: {@value}
   */
  public static final String DEFAULT_VALUE_SEPARATOR = ":";

  /**
   * 默认实例
   */
  public static final PlaceHolderHelper DEFAULT_HELPER;

  private static final Map<String, String> SIMPLE_PREFIXES = new HashMap<>(4);

  static {
    SIMPLE_PREFIXES.put("}", "{");
    SIMPLE_PREFIXES.put("]", "[");
    SIMPLE_PREFIXES.put(")", "(");

    DEFAULT_HELPER = new PlaceHolderHelper(DEFAULT_PLACEHOLDER_PREFIX, DEFAULT_PLACEHOLDER_SUFFIX,
        DEFAULT_VALUE_SEPARATOR, true);
  }

  private final String placeholderPrefix;

  private final String placeholderSuffix;

  private final String simplePrefix;

  private final String valueSeparator;

  private final boolean ignoreUnresolvablePlaceholders;

  public PlaceHolderHelper(String placeholderPrefix, String placeholderSuffix) {
    this(placeholderPrefix, placeholderSuffix, null, true);
  }

  public PlaceHolderHelper(String placeholderPrefix, String placeholderSuffix,
      String valueSeparator, boolean ignoreUnresolvablePlaceholders) {

    Checks.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
    Checks.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
    this.placeholderPrefix = placeholderPrefix;
    this.placeholderSuffix = placeholderSuffix;
    String simplePrefixForSuffix = SIMPLE_PREFIXES.get(this.placeholderSuffix);
    if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
      this.simplePrefix = simplePrefixForSuffix;
    } else {
      this.simplePrefix = this.placeholderPrefix;
    }
    this.valueSeparator = valueSeparator;
    this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
  }

  public static PlaceHolderHelper defaultInstance() {
    return DEFAULT_HELPER;
  }

  /**
   * 替换所有占位格式 {@code ${name}}
   *
   * @param value 需要转换的属性
   * @param properties 配置集合
   * @return 替换后的字符串
   */
  public String replacePlaceholders(String value, final Properties properties) {
    Checks.notNull(properties, "'properties' must not be null");
    Checks.notNull(value, "'value' must not be null");
    return parseStringValue(value, properties, new HashSet<>());
  }

  protected String parseStringValue(String value, Properties properties,
      Set<String> visitedPlaceholders) {
    StringBuilder result = new StringBuilder(value);
    int startIndex = value.indexOf(this.placeholderPrefix);
    while (startIndex != -1) {
      int endIndex = findPlaceholderEndIndex(result, startIndex);
      if (endIndex != -1) {
        String placeholder =
            result.substring(startIndex + this.placeholderPrefix.length(), endIndex);
        String originalPlaceholder = placeholder;
        if (!visitedPlaceholders.add(originalPlaceholder)) {
          throw new IllegalArgumentException("Circular placeholder reference '"
              + originalPlaceholder + "' in property definitions");
        }
        // 递归
        placeholder = parseStringValue(placeholder, properties, visitedPlaceholders);
        // 获取没有占位的属性值
        String propVal = properties.getProperty(placeholder);
        propVal = getCommonVal(properties, placeholder, propVal);
        if (propVal != null) {
          // 递归 表达式中含有表达式
          propVal = parseStringValue(propVal, properties, visitedPlaceholders);
          result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
          startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
        } else if (this.ignoreUnresolvablePlaceholders) {
          // 继续解析
          startIndex =
              result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
        } else {
          throw new IllegalArgumentException(String
              .format("Could not resolve placeholder '%s' in value '%s'", placeholder, value));
        }
        visitedPlaceholders.remove(originalPlaceholder);
      } else {
        startIndex = -1;
      }
    }
    return result.toString();
  }

  private String getCommonVal(Properties properties, String placeholder, String propVal) {
    if (propVal == null && this.valueSeparator != null) {
      int separatorIndex = placeholder.indexOf(this.valueSeparator);
      if (separatorIndex != -1) {
        String actualPlaceholder = placeholder.substring(0, separatorIndex);
        String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
        propVal = properties.getProperty(actualPlaceholder);
        if (propVal == null) {
          propVal = defaultValue;
        }
      }
    }
    return propVal;
  }

  private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
    int index = startIndex + this.placeholderPrefix.length();
    int withinNestedPlaceholder = 0;
    while (index < buf.length()) {
      if (substringMatch(buf, index, this.placeholderSuffix)) {
        if (withinNestedPlaceholder > 0) {
          withinNestedPlaceholder--;
          index = index + this.placeholderSuffix.length();
        } else {
          return index;
        }
      } else if (substringMatch(buf, index, this.simplePrefix)) {
        withinNestedPlaceholder++;
        index = index + this.simplePrefix.length();
      } else {
        index++;
      }
    }
    return -1;
  }

  private static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
    if (index + substring.length() > str.length()) {
      return false;
    }
    for (int i = 0; i < substring.length(); i++) {
      if (str.charAt(index + i) != substring.charAt(i)) {
        return false;
      }
    }
    return true;
  }

}
