/*
 * Copyright (C) 2021 the original author or authors.
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
package vip.justlive.oxygen.core.util.base;

import lombok.experimental.UtilityClass;

/**
 * 十进制转任意进制
 *
 * @author wubo
 */
@UtilityClass
public class DecimalSystemConvert {

  private final char[] DIGITS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
      'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
  };


  /**
   * 进制转换，默认使用数字+字母数组做为数据源
   *
   * @param value 原始数据
   * @param shift 进制数
   * @return 结果
   */
  public String convert(int value, int shift) {
    return convert(value, shift, DIGITS);
  }

  /**
   * 进制转换
   *
   * @param value  原始数据
   * @param shift  进制数
   * @param source 进制数组源
   * @return 结果
   */
  public String convert(int value, int shift, char[] source) {
    int temp = value;

    int len = (int) (Math.log(value) / Math.log(shift));
    if (temp % shift == 0) {
      len--;
    }

    char[] buf = new char[len + 1];

    while (temp > 0) {
      buf[len--] = (source[temp % shift]);
      temp /= shift;
    }
    return new String(buf);
  }

  /**
   * 恢复成十进制,默认使用数字+字母数组做为数据源
   *
   * @param value 进制转换后的值
   * @param shift 进制
   * @return 十进制
   */
  public int recover(String value, int shift) {
    return recover(value, shift, DIGITS);
  }

  /**
   * 恢复成十进制
   *
   * @param value  进制转换后的值
   * @param shift  进制
   * @param source 进制数组源
   * @return 十进制
   */
  public int recover(String value, int shift, char[] source) {
    char[] buf = value.toCharArray();
    int result = 0;
    for (int i = buf.length - 1; i >= 0; i--) {
      int index = indexOf(buf[i], shift, source);
      if (index == -1) {
        throw new IllegalArgumentException();
      }
      result += ((int) Math.pow(shift, (buf.length - i - 1))) * index;
    }
    return result;
  }

  private int indexOf(char c, int shift, char[] source) {
    for (int i = 0; i < shift; i++) {
      if (source[i] == c) {
        return i;
      }
    }
    return -1;
  }

}
