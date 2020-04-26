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
package vip.justlive.oxygen.core.util.retry;

import lombok.Getter;

/**
 * 尝试
 *
 * @param <T> 泛型
 * @author wubo
 */
@Getter
public class Attempt<T> {

  /**
   * 尝试次数
   */
  private final long attemptNumber;

  /**
   * 结果
   */
  private final T result;

  /**
   * 距离第一次尝试时间
   */
  private final long millsAfterFirstAttempt;

  /**
   * 异常
   */
  private final Exception exception;

  public Attempt(long attemptNumber, T result, long millsAfterFirstAttempt) {
    this.attemptNumber = attemptNumber;
    this.result = result;
    this.millsAfterFirstAttempt = millsAfterFirstAttempt;
    this.exception = null;
  }

  public Attempt(long attemptNumber, Exception exception, long millsAfterFirstAttempt) {
    this.attemptNumber = attemptNumber;
    this.exception = exception;
    this.millsAfterFirstAttempt = millsAfterFirstAttempt;
    this.result = null;
  }

  /**
   * 存在异常
   *
   * @return true表示存在
   */
  public boolean hasException() {
    return exception != null;
  }

  /**
   * 获取结果
   *
   * @return result
   */
  public T getResult() {
    if (hasException()) {
      throw new IllegalStateException("当前尝试存在异常，没有返回值", exception);
    }
    return result;
  }

}
