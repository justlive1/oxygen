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
package vip.justlive.oxygen.core.exception;

import lombok.Getter;

/**
 * 编码异常
 *
 * @author wubo
 */
@Getter
public class CodedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * 该异常的错误码
   */
  private final ErrorCode errorCode;
  private final transient Object[] args;
  private final transient Object data;

  public CodedException(Throwable throwable, ErrorCode errorCode, Object[] args) {
    this(throwable, errorCode, args, null);
  }

  public CodedException(Throwable throwable, ErrorCode errorCode, Object[] args, Object data) {
    super(throwable);
    this.errorCode = errorCode;
    this.args = args;
    this.data = data;
  }

  public CodedException(ErrorCode errorCode) {
    this(errorCode, null, null);
  }

  public CodedException(ErrorCode errorCode, Object[] args) {
    this(errorCode, args, null);
  }

  public CodedException(ErrorCode errorCode, Object[] args, Object data) {
    this.errorCode = errorCode;
    this.args = args;
    this.data = data;
  }

  @Override
  public String getMessage() {
    if (errorCode != null) {
      return errorCode.toString();
    }
    return super.getMessage();
  }

  @Override
  public String toString() {
    if (errorCode == null) {
      return super.toString();
    }
    return errorCode.toString();
  }

}
