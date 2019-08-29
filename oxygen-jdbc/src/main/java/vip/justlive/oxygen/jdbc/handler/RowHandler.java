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
package vip.justlive.oxygen.jdbc.handler;

import java.sql.ResultSet;
import java.util.Map;

/**
 * 数据行处理
 *
 * @author wubo
 */
public interface RowHandler {

  /**
   * 转换成数组
   *
   * @param rs 结果集
   * @return 数据
   */
  Object[] toArray(ResultSet rs);

  /**
   * 转换为map
   *
   * @param rs 结果集
   * @return map
   */
  Map<String, Object> toMap(ResultSet rs);

  /**
   * 转换为对象
   *
   * @param rs 结果集
   * @param type 对象类型
   * @param <T> 泛型
   * @return 对象
   */
  <T> T toBean(ResultSet rs, Class<T> type);
}
