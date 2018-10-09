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
package vip.justlive.oxygen.web.mapping;

import lombok.Data;

/**
 * 数据绑定
 *
 * @author wubo
 */
@Data
public class DataBinder {

  /**
   * 数据绑定名称
   */
  private String name;
  /**
   * 数据类型
   */
  private Class<?> type;
  /**
   * 默认值
   */
  private String defaultValue;
  /**
   * 数据获取的范围, 默认从参数中取值
   */
  private SCOPE scope = SCOPE.PARAM;

  /**
   * 数据获取范围
   */
  public enum SCOPE {
    /**
     * 参数
     */
    PARAM,
    /**
     * 头信息
     */
    HEADER,
    /**
     * cookie
     */
    COOKIE,
    /**
     * session
     */
    SESSION,
    /**
     * 请求路径
     */
    PATH
  }
}
