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
package vip.justlive.oxygen.core.exception;

import vip.justlive.oxygen.core.constant.Constants;

/**
 * 用于创建CodedException
 * <p>
 * fail方法创建的异常为业务逻辑异常，不含堆栈信息； fault方法创建的异常为故障型异常，包含堆栈信息
 * </p>
 *
 * @author wubo
 */
public class Exceptions {

  private Exceptions() {
  }

  /**
   * 抛出unchecked异常
   *
   * @param e 异常
   * @return 包装异常
   */
  public static CodedException wrap(Throwable e) {
    if (e instanceof CodedException) {
      return (CodedException) e;
    }
    return new CodedException(e, null);
  }


  /**
   * 抛出unchecked异常
   *
   * @param e 异常
   * @param message 异常消息
   * @return 包装异常
   */
  public static CodedException wrap(Throwable e, String message) {
    return new CodedException(e, errorMessage(message));
  }

  /**
   * 抛出unchecked异常
   *
   * @param e 异常
   * @param code 异常编码
   * @param message 异常消息
   * @return 包装异常
   */
  public static CodedException wrap(Throwable e, String code, String message) {
    return new CodedException(e, errorMessage(Constants.DEFAULT_NODULE, code, message));
  }

  /**
   * 抛出unchecked异常
   *
   * @param e 异常
   * @param errorCode 异常编码包装
   * @param arguments 参数
   * @return 包装异常
   */
  public static CodedException wrap(Throwable e, ErrorCode errorCode, Object... arguments) {
    return new CodedException(e, errorCode, arguments);
  }

  /**
   * 创建带错误提示信息的ErrorCode
   *
   * @param message 消息
   * @return 异常编码包装
   */
  public static ErrorCode errorMessage(String message) {
    return errorMessage(Constants.DEFAULT_FAIL_CODE, message);
  }

  /**
   * 创建带错误提示信息的ErrorCode
   *
   * @param code 编码
   * @param message 消息
   * @return 异常编码包装
   */
  public static ErrorCode errorMessage(String code, String message) {
    return errorMessage(Constants.DEFAULT_NODULE, code, message);
  }

  /**
   * 创建带错误提示信息的ErrorCode
   *
   * @param module 模块
   * @param code 编码
   * @param message 消息
   * @return 异常编码包装
   */
  public static ErrorCode errorMessage(String module, String code, String message) {
    return new ErrorCode(module, code, message);
  }

  /**
   * 创建可带参数的业务逻辑异常
   *
   * @param message 异常消息
   * @return 包装异常
   */
  public static CodedException fail(String message) {
    return fail(Constants.DEFAULT_FAIL_CODE, message);
  }

  /**
   * 创建可带参数的业务逻辑异常
   *
   * @param code 异常编码
   * @param message 异常消息
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fail(String code, String message, Object... params) {
    return fail(errorMessage(code, message), params);
  }

  /**
   * 创建可带参数的业务逻辑异常
   *
   * @param errCode 异常编码包装
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fail(ErrorCode errCode, Object... params) {
    return new NoStackException(errCode, params);
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param message 异常消息
   * @return 包装异常
   */
  public static CodedException fault(String message) {
    return fault(errorMessage(message));
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param code 异常编码
   * @param message 异常消息
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fault(String code, String message, Object... params) {
    return fault(errorMessage(code, message), params);
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param e 异常
   * @param code 异常编码
   * @param message 异常消息
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fault(Throwable e, String code, String message, Object... params) {
    return fault(e, errorMessage(code, message), params);
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param errCode 异常编码包装
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fault(ErrorCode errCode, Object... params) {
    return new CodedException(errCode, params);
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param e 异常
   * @param errCode 异常编码包装
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fault(Throwable e, ErrorCode errCode, Object... params) {
    return new CodedException(e, errCode, params);
  }
}
