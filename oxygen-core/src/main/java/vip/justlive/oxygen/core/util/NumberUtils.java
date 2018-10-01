/*
 * Copyright (C) 2018 justlive1
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package vip.justlive.oxygen.core.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  public static final List<Class<? extends Number>> STANDARD_NUMBER_TYPES;

  private static final Map<Class<?>, Function<String, ?>> FUNCTIONS;

  static {

    STANDARD_NUMBER_TYPES = Arrays
        .asList(Byte.class, Short.class, Integer.class, Long.class, BigInteger.class, Float.class,
            Double.class, BigDecimal.class);

    FUNCTIONS = new HashMap<>(16, 1f);
    FUNCTIONS.put(Byte.class,
        trimmed -> (isHexNumber(trimmed) ? Byte.decode(trimmed) : Byte.valueOf(trimmed)));
    FUNCTIONS.put(Short.class,
        trimmed -> (isHexNumber(trimmed) ? Short.decode(trimmed) : Short.valueOf(trimmed)));
    FUNCTIONS.put(Integer.class,
        trimmed -> (isHexNumber(trimmed) ? Integer.decode(trimmed) : Integer.valueOf(trimmed)));
    FUNCTIONS.put(Long.class,
        trimmed -> (isHexNumber(trimmed) ? Long.decode(trimmed) : Long.valueOf(trimmed)));
    FUNCTIONS.put(BigInteger.class,
        trimmed -> (isHexNumber(trimmed) ? decodeBigInteger(trimmed) : new BigInteger(trimmed)));
    FUNCTIONS.put(Float.class, Float::valueOf);
    FUNCTIONS.put(Double.class, Double::valueOf);
    FUNCTIONS.put(BigDecimal.class, NumberUtils::stringToBigDecimal);
    FUNCTIONS.put(Number.class, NumberUtils::stringToBigDecimal);
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
