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
import java.util.List;
import java.util.Map;

/**
 * 结果集处理
 *
 * @author wubo
 */
public interface ResultSetHandler<T> {

  /**
   * 处理结果集
   *
   * @param rs 结果集
   * @return bean
   */
  T handle(ResultSet rs);

  /**
   * map处理
   *
   * @return MapResultSetHandler
   */
  static ResultSetHandler<Map<String, Object>> mapHandler() {
    return MapResultSetHandler.INSTANCE;
  }

  /**
   * mapList处理
   *
   * @return MapListResultSetHandler
   */
  static ResultSetHandler<List<Map<String, Object>>> mapListHandler() {
    return MapListResultSetHandler.INSTANCE;
  }

  /**
   * array处理
   *
   * @return ArrayResultSetHandler
   */
  static ResultSetHandler<Object[]> arrayHandler() {
    return ArrayResultSetHandler.INSTANCE;
  }

  /**
   * array List处理
   *
   * @return ArrayListResultSetHandler
   */
  static ResultSetHandler<List<Object[]>> arrayListHandler() {
    return ArrayListResultSetHandler.INSTANCE;
  }

  /**
   * bean处理
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return BeanResultSetHandler
   */
  static <T> ResultSetHandler<T> beanHandler(Class<T> clazz) {
    return new BeanResultSetHandler<>(clazz);
  }

  /**
   * beanList处理
   *
   * @param clazz 类型
   * @param <T> 泛型
   * @return BeanListResultSetHandler
   */
  static <T> ResultSetHandler<List<T>> beanListHandler(Class<T> clazz) {
    return new BeanListResultSetHandler<>(clazz);
  }
}
