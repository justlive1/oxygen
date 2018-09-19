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

/**
 * 不带堆栈的异常
 *
 * @author wubo
 */
public class NoStackException extends CodedException {

  private static final long serialVersionUID = 1L;

  /**
   * 构造方法
   *
   * @param errorCode 异常编码包装
   * @param args 参数
   */
  NoStackException(ErrorCode errorCode, Object... args) {
    super(errorCode, args);
  }

  /**
   * 覆盖该方法，以提高服务层异常Runtime时的执行效率
   * <br>
   * 不需要声明方法为父类的synchronized
   */
  @Override
  public Throwable fillInStackTrace() {
    return this;
  }
}
