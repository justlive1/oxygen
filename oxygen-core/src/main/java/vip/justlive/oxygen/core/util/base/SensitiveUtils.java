/*
 * Copyright (C) 2020 the original author or authors.
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
 * 敏感信息工具类
 *
 * @author wubo
 */
@UtilityClass
public class SensitiveUtils {

  private final int BANK_CARD_LENGTH = 16;
  private final int PHONE_LENGTH = 11;

  /**
   * 只适用于中文姓名
   *
   * @param data 姓名
   * @return 返回按规范进行部分隐藏后的姓名
   */
  public String nameHide(final String data) {
    return customizeHide(data, 1, 0, 2);
  }

  /**
   * 对大陆身份证号进行部分隐藏处理，只显示前2位和后2位，其他用*代替。
   *
   * @param data 待部分隐藏处理的身份证号。
   * @return 返回身份证号部分展示字符串
   */
  public String idCardNoHide(String data) {
    if (!Strings.hasText(data)) {
      return data;
    }
    return customizeHide(data, 2, 2, data.length() - 4);
  }

  /**
   * 对银行卡号进行部分隐藏处理， 如果银行卡位数大于等于16位只显示前6位和后4位，其他用*代替。
   * <br>
   * 如果银行卡位数小于16位，则使用缺省脱敏方式。
   *
   * @param data 待部分隐藏处理的银行卡号。
   * @return 返回银行卡部分展示字符串
   */
  public String bankCardNoHide(String data) {
    if (!Strings.hasText(data)) {
      return data;
    }
    if (data.length() >= BANK_CARD_LENGTH) {
      return customizeHide(data, 6, 4, data.length() - 10);
    } else {
      return defaultHide(data);
    }
  }

  /**
   * 手机号码通用隐藏规则（包括港澳台地区），隐藏中间四位 适用于网站以及客户端
   * <ul>
   * <li>SensitiveUtils.cellphoneHide("13071835358") = 130******58
   * <li>SensitiveUtils.cellphoneHide("3071835358") = 307****358
   * <li>SensitiveUtils.cellphoneHide("071835358") = 07****358
   * <li>SensitiveUtils.cellphoneHide("835358") = 8****8
   * </ul>
   *
   * @param data 手机号
   * @return 隐藏后的手机号码
   */
  public String cellphoneHide(String data) {
    if (!Strings.hasText(data)) {
      return data;
    }
    String tmp = data.trim();
    // 如果是国内手机号，则前三后二
    if (tmp.length() == PHONE_LENGTH) {
      return customizeHide(tmp, 3, 2, 6);
    }
    int notHideNum = tmp.length() - 4;
    return customizeHide(tmp, notHideNum / 2, notHideNum - notHideNum / 2, 4);
  }

  /**
   * 对Email进行部分隐藏处理，只显示用户名的前3位+***+@域名。如用户名不足3位，将显示用户名全部+***+@域名。
   * <br>
   * 如果传入的数据不是email（不含‘@’）,将按敏感信息缺省隐藏方式处理，显示前1/3和后1/3
   *
   * @param data Email
   * @return 隐藏后的email
   */
  public String emailHide(String data) {
    if (!Strings.hasText(data)) {
      return data;
    }

    String tmp = data.trim();
    int atPos = tmp.indexOf(Strings.AT);
    if (atPos == -1) {
      return defaultHide(data);
    }
    int frontNum = Math.min(atPos, 3);
    return customizeHide(tmp, frontNum, tmp.length() - atPos, 3);
  }

  /**
   * 敏感信息缺省隐藏方式处理：显示前1/3和后1/3，其他*号代替。内容长度不能被3整除时，显示前ceil[length/3.0]和后floor[ length/3.0]
   *
   * <pre>
   * SensitiveUtils.defaultHide("ttt") = "t*t"
   * SensitiveUtils.defaultHide("12345678") = "123***78"
   * </pre>
   *
   * @param data 待部分隐藏处理的敏感信息。
   * @return 屏蔽后的数据
   */
  public String defaultHide(String data) {
    if (!Strings.hasText(data)) {
      return data;
    }
    String tmp = data.trim();
    int length = tmp.length();
    int headNum = (int) Math.ceil(length / 3.0);
    int tailNum = (int) Math.floor(length / 3.0);
    return customizeHide(tmp, headNum, tailNum, length - headNum - tailNum);
  }

  /**
   * 自定义屏蔽位数和屏蔽位置
   *
   * <pre>
   * SensitiveUtils.customizeHide("13568794561",3,4,4) = "135****4561"
   * SensitiveUtils.customizeHide("13568794561",0,4,4) = "****4561"
   * SensitiveUtils.customizeHide("13568794561",3,0,4) = "135****"
   * SensitiveUtils.customizeHide("13568794561",3,0,8) = "135********"
   * </pre>
   *
   * @param data 原数据
   * @param front 展示前几位
   * @param tail 展示后几位
   * @param hidden 展示星号*的个数
   * @return 部分隐藏的敏感数据字符串
   */
  public String customizeHide(String data, int front, int tail, int hidden) {
    if (!Strings.hasText(data)) {
      return data;
    }
    String tmp = data.trim();
    int length = tmp.length();
    // 合法性检查，如果参数不合法，返回源数据内容
    if (front < 0 || tail < 0 || hidden < 0 || front + tail > length) {
      return tmp;
    }
    int beginIndex = front - 1;
    int endIndex = length - tail;
    // 原数据前半部分
    StringBuilder result = new StringBuilder();
    if (beginIndex >= 0 && beginIndex < length) {
      result.append(tmp, 0, front);
    }
    // 中间*
    for (int i = 0; i < hidden; i++) {
      result.append(Strings.ANY);
    }
    // 原数据后半部分
    if (endIndex >= 0 && endIndex < length) {
      result.append(tmp.substring(endIndex));
    }
    return result.toString();
  }
}
