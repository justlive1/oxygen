/*
 * Copyright (C) 2019 the original author or authors.
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

package vip.justlive.oxygen.core.domain;

import vip.justlive.oxygen.core.constant.Constants;

/**
 * 返回
 *
 * @param <T> 泛型
 * @author wubo
 * @since 2.0.4
 */
public class RespVo<T> extends RespData<T> {

  private static final long serialVersionUID = 1L;

  /**
   * 成功返回
   *
   * @param <T> 泛型
   * @return 返回实体
   */
  public static <T> RespVo<T> success() {
    return success(null);
  }

  /**
   * 成功返回
   *
   * @param data 数据
   * @param <T> 泛型
   * @return 返回实体
   */
  public static <T> RespVo<T> success(T data) {
    RespVo<T> resp = new RespVo<>();
    resp.setData(data).setCode(Constants.SUCCESS_CODE);
    return resp;
  }

  /**
   * 失败返回
   *
   * @param message 消息
   * @param <T> 泛型
   * @return 返回实体
   */
  public static <T> RespVo<T> error(String message) {
    return error(Constants.DEFAULT_FAIL_CODE, message);
  }

  /**
   * 失败返回
   *
   * @param code 编码
   * @param message 消息
   * @param <T> 泛型
   * @return 返回实体
   */
  public static <T> RespVo<T> error(String code, String message) {
    RespVo<T> resp = new RespVo<>();
    resp.setCode(code).setMessage(message);
    return resp;
  }

  /**
   * 是否成功
   *
   * @return 是否成功
   */
  public boolean isSuccess() {
    return Constants.SUCCESS_CODE.equals(getCode());
  }
}
