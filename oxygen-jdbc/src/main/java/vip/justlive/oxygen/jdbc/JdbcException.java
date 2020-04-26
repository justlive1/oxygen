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
package vip.justlive.oxygen.jdbc;

/**
 * jdbc包装异常
 *
 * @author wubo
 */
public class JdbcException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public JdbcException(Throwable e) {
    super(e);
  }

  public JdbcException(String msg) {
    super(msg);
  }

  /**
   * 封装异常
   *
   * @param e 异常
   * @return jdbcException
   */
  public static JdbcException wrap(Throwable e) {
    return new JdbcException(e);
  }

}
