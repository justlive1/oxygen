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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * java8 date
 *
 * @author wubo
 */
public class Java8DatePropertyHandler implements PropertyHandler {

  private static final List<Class<?>> TYPES = Arrays.asList(Instant.class, JapaneseDate.class,
      LocalDateTime.class, LocalDate.class, LocalTime.class, OffsetDateTime.class,
      OffsetTime.class, ZonedDateTime.class);

  @Override
  public boolean supported(Class<?> type, Object value) {
    if (!(value instanceof Date)) {
      return false;
    }
    return TYPES.contains(type);
  }

  @Override
  public Object cast(Class<?> type, Object value) {
    Timestamp timestamp = new Timestamp(((Date) value).getTime());
    if (type == Instant.class) {
      value = timestamp.toInstant();
    } else if (type == JapaneseDate.class) {
      value = JapaneseDate.from(timestamp.toLocalDateTime());
    } else if (type == LocalDateTime.class) {
      value = timestamp.toLocalDateTime();
    } else if (type == LocalDate.class) {
      value = timestamp.toLocalDateTime().toLocalDate();
    } else if (type == LocalTime.class) {
      value = timestamp.toLocalDateTime().toLocalTime();
    } else if (type == OffsetDateTime.class) {
      value = OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
    } else if (type == OffsetTime.class) {
      value = timestamp.toLocalDateTime().atOffset(OffsetDateTime.now().getOffset()).toOffsetTime();
    } else if (type == ZonedDateTime.class) {
      value = ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
    }
    return value;
  }

}
