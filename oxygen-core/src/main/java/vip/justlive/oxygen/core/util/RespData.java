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
package vip.justlive.oxygen.core.util;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 返回
 *
 * @param <T> 泛型
 * @author wubo
 * @since 2.0.0
 */
@Data
@Accessors(chain = true)
public class RespData<T> implements Serializable {

  /**
   * 成功code
   */
  public static final String SUCCESS_CODE = "00000";
  /**
   * 失败code
   */
  public static final String DEFAULT_FAIL_CODE = "99999";

  private static final long serialVersionUID = 1L;
  /**
   * 返回结果编码
   */
  private String code;

  /**
   * 结果描述信息
   */
  private String message;

  /**
   * T 返回数据
   */
  @SuppressWarnings("squid:S1948")
  private T data;

}
