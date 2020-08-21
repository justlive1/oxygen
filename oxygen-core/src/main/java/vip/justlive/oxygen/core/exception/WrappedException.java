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
 * 包装异常
 *
 * @author wubo
 */
public class WrappedException extends CodedException {

  private static final long serialVersionUID = 3109011947026387897L;

  @Getter
  private final Throwable exception;

  public WrappedException(Throwable exception) {
    this(exception, null);
  }

  public WrappedException(Throwable exception, ErrorCode errorCode) {
    super(errorCode);
    this.exception = exception;
    addSuppressed(this.exception);
  }

  @Override
  public String getMessage() {
    return String.format("Wrapper of [%s]: %s", exception, exception.getMessage());
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
