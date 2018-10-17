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
package vip.justlive.oxygen.jdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 列处理器
 *
 * @author wubo
 */
public interface ColumnHandler {

  /**
   * 是否支持该类型处理
   *
   * @param type type
   * @return true is supported
   */
  boolean supported(Class<?> type);

  /**
   * 获取转换数据
   *
   * @param rs 结果集
   * @param index 下标
   * @return 数据
   * @throws SQLException sql异常
   */
  Object fetch(ResultSet rs, int index) throws SQLException;
}
