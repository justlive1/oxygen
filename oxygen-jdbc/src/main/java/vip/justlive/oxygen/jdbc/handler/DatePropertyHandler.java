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

package vip.justlive.oxygen.jdbc.handler;

import java.sql.Date;
import java.sql.Time;

/**
 * date 类型转换
 *
 * @author wubo
 */
public class DatePropertyHandler implements PropertyHandler {

  @Override
  public boolean supported(Class<?> type, Object value) {
    if (!(value instanceof java.util.Date)) {
      return false;
    }
    return type == Date.class || type == Time.class;
  }

  @Override
  public Object cast(Class<?> type, Object value) {
    java.util.Date date = (java.util.Date) value;
    if (type == Date.class) {
      value = new Date(date.getTime());
    } else if (type == Time.class) {
      value = new Time(date.getTime());
    }
    return value;
  }
}
