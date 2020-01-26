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
package vip.justlive.oxygen.core.exception;

import lombok.experimental.UtilityClass;

/**
 * 用于创建CodedException
 * <p>
 * fail方法创建的异常为业务逻辑异常，不含堆栈信息； fault方法创建的异常为故障型异常，包含堆栈信息
 * </p>
 *
 * @author wubo
 */
@UtilityClass
public class Exceptions {

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
    return new WrappedException(e);
  }


  /**
   * 抛出unchecked异常
   *
   * @param e 异常
   * @param message 异常消息
   * @return 包装异常
   */
  public static CodedException wrap(Throwable e, String message) {
    return wrap(e, errorMessage(message));
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
    return wrap(e, errorMessage(code, message));
  }

  /**
   * 抛出unchecked异常
   *
   * @param e 异常
   * @param errorCode 异常编码包装
   * @return 包装异常
   */
  public static CodedException wrap(Throwable e, ErrorCode errorCode) {
    if (e instanceof CodedException) {
      return (CodedException) e;
    }
    return new WrappedException(e, errorCode);
  }

  /**
   * 创建带错误提示信息的ErrorCode
   *
   * @param message 消息
   * @return 异常编码包装
   */
  public static ErrorCode errorMessage(String message) {
    return new ErrorCode(message);
  }

  /**
   * 创建带错误提示信息的ErrorCode
   *
   * @param code 编码
   * @param message 消息
   * @return 异常编码包装
   */
  public static ErrorCode errorMessage(String code, String message) {
    return new ErrorCode(code, message);
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
   * 创建业务逻辑异常
   *
   * @param message 异常消息
   * @return 包装异常
   */
  public static CodedException fail(String message) {
    return fail(errorMessage(message));
  }

  /**
   * 创建可带参数的业务逻辑异常
   *
   * @param code 异常编码
   * @param message 异常消息
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException fail(String code, String message, Object... args) {
    return fail(errorMessage(code, message), args);
  }

  /**
   * 创建可带参数的业务逻辑异常
   *
   * @param errCode 异常编码包装
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException fail(ErrorCode errCode, Object... args) {
    return new NoStackException(errCode, args);
  }

  /**
   * 创建可带扩展数据的业务逻辑异常
   *
   * @param message 异常消息
   * @param data 扩展数据
   * @return 包装异常
   */
  public static CodedException failWithData(String message, Object data) {
    return failWithData(errorMessage(message), data);
  }

  /**
   * 创建可带参数和扩展数据的业务逻辑异常
   *
   * @param code 异常编码
   * @param message 异常消息
   * @param data 扩展数据
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException failWithData(String code, String message, Object data,
      Object... args) {
    return failWithData(errorMessage(code, message), args, data);
  }

  /**
   * 创建可带参数和扩展数据的业务逻辑异常
   *
   * @param errCode 异常编码包装
   * @param data 扩展数据
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException failWithData(ErrorCode errCode, Object data, Object... args) {
    return new NoStackException(errCode, args, data);
  }

  /**
   * 创建故障异常
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
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException fault(String code, String message, Object... args) {
    return fault(errorMessage(code, message), args);
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param e 异常
   * @param code 异常编码
   * @param message 异常消息
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException fault(Throwable e, String code, String message, Object... args) {
    return fault(e, errorMessage(code, message), args);
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param errCode 异常编码包装
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException fault(ErrorCode errCode, Object... args) {
    return new CodedException(errCode, args);
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param e 异常
   * @param errCode 异常编码包装
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException fault(Throwable e, ErrorCode errCode, Object... args) {
    return new CodedException(e, errCode, args);
  }

  /**
   * 创建可带扩展数据故障异常
   *
   * @param message 异常消息
   * @param data 扩展数据
   * @return 包装异常
   */
  public static CodedException faultWithData(String message, Object data) {
    return faultWithData(errorMessage(message), data);
  }

  /**
   * 创建可带参数和扩展数据的故障异常
   *
   * @param code 异常编码
   * @param message 异常消息
   * @param data 扩展数据
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException faultWithData(String code, String message, Object data,
      Object... args) {
    return faultWithData(errorMessage(code, message), args, data);
  }

  /**
   * 创建可带参数和扩展数据的故障异常
   *
   * @param e 异常
   * @param code 异常编码
   * @param message 异常消息
   * @param data 扩展数据
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException faultWithData(Throwable e, String code, String message, Object data,
      Object... args) {
    return faultWithData(e, errorMessage(code, message), args, data);
  }

  /**
   * 创建可带参数和扩展数据的故障异常
   *
   * @param errCode 异常编码包装
   * @param data 扩展数据
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException faultWithData(ErrorCode errCode, Object data, Object... args) {
    return new CodedException(errCode, args, data);
  }

  /**
   * 创建可带参数和扩展数据的故障异常
   *
   * @param e 异常
   * @param errCode 异常编码包装
   * @param data 扩展数据
   * @param args 参数
   * @return 包装异常
   */
  public static CodedException faultWithData(Throwable e, ErrorCode errCode, Object data,
      Object... args) {
    return new CodedException(e, errCode, args, data);
  }
}
