package vip.justlive.oxygen.core.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 数字工具类
 *
 * @author wubo
 */
public class NumberUtils {

  /**
   * jdk标准Number类型
   * <br>
   * Byte, Short, Integer, Long, BigInteger, Float, Double, BigDecimal
   */
  public static final Set<Class<? extends Number>> STANDARD_NUMBER_TYPES;

  private static final Map<Class<?>, Function<String, ?>> FUNCTIONS;

  static {

    STANDARD_NUMBER_TYPES = ImmutableSet.<Class<? extends Number>>builder().add(Byte.class)
        .add(Short.class).add(Integer.class).add(Long.class).add(BigInteger.class).add(Float.class)
        .add(Double.class).add(BigDecimal.class).build();

    FUNCTIONS = ImmutableMap.<Class<?>, Function<String, ?>>builder().put(Byte.class,
        trimmed -> (isHexNumber(trimmed) ? Byte.decode(trimmed) : Byte.valueOf(trimmed)))
        .put(Short.class,
            trimmed -> (isHexNumber(trimmed) ? Short.decode(trimmed) : Short.valueOf(trimmed)))
        .put(Integer.class,
            trimmed -> (isHexNumber(trimmed) ? Integer.decode(trimmed) : Integer.valueOf(trimmed)))
        .put(Long.class,
            trimmed -> (isHexNumber(trimmed) ? Long.decode(trimmed) : Long.valueOf(trimmed)))
        .put(BigInteger.class,
            trimmed -> (isHexNumber(trimmed) ? decodeBigInteger(trimmed) : new BigInteger(trimmed)))
        .put(Float.class, Float::valueOf).put(Double.class, Double::valueOf)
        .put(BigDecimal.class, NumberUtils::stringToBigDecimal)
        .put(Number.class, NumberUtils::stringToBigDecimal).build();
  }

  private NumberUtils() {
  }

  /**
   * 解析提供的 {@code text} 字符串，转换成目标类型
   * <p>
   * 解析之前先去了除首尾空格
   * <p>
   * 支持十六进制 ("0x", "0X", "#").
   *
   * @param text 文本
   * @param targetClass 目标类
   * @param <T> 泛型类
   * @return 转换后的类型
   * @throws IllegalArgumentException 当不上jdk标准的Number实现会抛出该异常
   */
  @SuppressWarnings("unchecked")
  public static <T extends Number> T parseNumber(String text, Class<T> targetClass) {
    Checks.notNull(text, "Text must not be null");
    Checks.notNull(targetClass, "Target class must not be null");
    String trimmed = text.trim();
    Function<String, ?> func = FUNCTIONS.get(targetClass);
    if (func == null) {
      throw new IllegalArgumentException(String
          .format("Cannot convert String [%s] to target class [%s]", text, targetClass.getName()));
    }
    return (T) func.apply(trimmed);
  }

  /**
   * 判断是否是十六进制
   *
   * @param value 校验值
   * @return true 是十六进制
   */
  public static boolean isHexNumber(String value) {
    int index = (value.startsWith("-") ? 1 : 0);
    return (value.startsWith("0x", index) || value.startsWith("0X", index) || value
        .startsWith("#", index));
  }

  /**
   * 将字符串解析成BigInteger
   *
   * @param value 解析值
   * @return 解析后的类型
   */
  public static BigInteger decodeBigInteger(String value) {
    int radix = 10;
    int index = 0;
    boolean negative = false;

    // 处理正负
    if (value.startsWith("-")) {
      negative = true;
      index++;
    }

    // 判断进制
    if (value.startsWith("0x", index) || value.startsWith("0X", index)) {
      index += 2;
      radix = 16;
    } else if (value.startsWith("#", index)) {
      index++;
      radix = 16;
    } else if (value.startsWith("0", index) && value.length() > 1 + index) {
      index++;
      radix = 8;
    }

    BigInteger result = new BigInteger(value.substring(index), radix);
    return (negative ? result.negate() : result);
  }

  public static BigDecimal stringToBigDecimal(String val) {
    return new BigDecimal(val);
  }
}
