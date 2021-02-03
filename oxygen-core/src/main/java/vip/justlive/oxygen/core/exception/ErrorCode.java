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

import java.io.Serializable;
import lombok.Data;

/**
 * 异常包装类
 *
 * @author wubo
 */
@Data
public class ErrorCode implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 模块
   */
  private String module = "ERROR";

  /**
   * 错误编码
   */
  private String code = "99999";

  /**
   * 错误信息
   */
  private String message;

  public ErrorCode(String message) {
    this.message = message;
  }

  public ErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public ErrorCode(String module, String code, String message) {
    this.module = module;
    this.code = code;
    this.message = message;
  }

  @Override
  public String toString() {
    return String.format("[%s][%s][%s]", module, code, message);
  }
}
