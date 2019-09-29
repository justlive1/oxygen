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
package vip.justlive.oxygen.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import lombok.Getter;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.Strings;

/**
 * Cron
 *
 * @author wubo
 */
public class CronExpression implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final Map<String, String> MONTH_MAP = new HashMap<>(12, 1);
  private static final Map<String, String> WEEK_MAP = new HashMap<>(7, 1);
  private static final String MONTH_VALUES = "JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV,DEC,FOO";
  private static final String WEEK_VALUES = "SUN,MON,TUE,WED,THU,FRI,SAT";
  private static final int MAX_DEEP = 4;
  private static final int CRON_FIELDS = 6;

  static {
    int i = 1;
    for (String month : MONTH_VALUES.split(Strings.COMMA)) {
      MONTH_MAP.put(month, String.valueOf(i++));
    }
    i = 0;
    for (String week : WEEK_VALUES.split(Strings.COMMA)) {
      WEEK_MAP.put(week, String.valueOf(i++));
    }
  }

  @Getter
  private final String expression;
  @Getter
  private final TimeZone timeZone;

  private final BitSet months = new BitSet(12);
  private final BitSet daysOfMonth = new BitSet(31);
  private final BitSet daysOfWeek = new BitSet(7);
  private final BitSet hours = new BitSet(24);
  private final BitSet minutes = new BitSet(60);
  private final BitSet seconds = new BitSet(60);


  public CronExpression(String expression) {
    this(expression, TimeZone.getDefault());
  }

  public CronExpression(String expression, TimeZone timeZone) {
    this.expression = expression;
    this.timeZone = timeZone;
    parse();
  }

  /**
   * Get the next {@link Date} in the sequence matching the Cron pattern and after the value
   * provided. The return value will have a whole number of seconds, and will be after the input
   * value.
   *
   * @param date a seed value
   * @return the next value matching the pattern
   */
  public Date next(Date date) {
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeZone(this.timeZone);
    calendar.setTime(date);

    // First, just reset the milliseconds and try to calculate from there...
    calendar.set(Calendar.MILLISECOND, 0);
    long originalTimestamp = calendar.getTimeInMillis();
    doNext(calendar, calendar.get(Calendar.YEAR));

    if (calendar.getTimeInMillis() == originalTimestamp) {
      // We arrived at the original timestamp - round up to the next whole second and try again...
      calendar.add(Calendar.SECOND, 1);
      doNext(calendar, calendar.get(Calendar.YEAR));
    }

    return calendar.getTime();
  }

  private void doNext(Calendar calendar, int dot) {
    List<Integer> resets = new ArrayList<>();

    int second = calendar.get(Calendar.SECOND);
    List<Integer> emptyList = Collections.emptyList();
    int updateSecond = findNext(this.seconds, second, calendar, Calendar.SECOND, Calendar.MINUTE,
        emptyList);
    if (second == updateSecond) {
      resets.add(Calendar.SECOND);
    }

    int minute = calendar.get(Calendar.MINUTE);
    int updateMinute = findNext(this.minutes, minute, calendar, Calendar.MINUTE,
        Calendar.HOUR_OF_DAY, resets);
    if (minute == updateMinute) {
      resets.add(Calendar.MINUTE);
    } else {
      doNext(calendar, dot);
    }

    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int updateHour = findNext(this.hours, hour, calendar, Calendar.HOUR_OF_DAY,
        Calendar.DAY_OF_WEEK, resets);
    if (hour == updateHour) {
      resets.add(Calendar.HOUR_OF_DAY);
    } else {
      doNext(calendar, dot);
    }

    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    int updateDayOfMonth = findNextDay(calendar, this.daysOfMonth, dayOfMonth, daysOfWeek,
        dayOfWeek, resets);
    if (dayOfMonth == updateDayOfMonth) {
      resets.add(Calendar.DAY_OF_MONTH);
    } else {
      doNext(calendar, dot);
    }

    int month = calendar.get(Calendar.MONTH);
    int updateMonth = findNext(this.months, month, calendar, Calendar.MONTH, Calendar.YEAR, resets);
    if (month != updateMonth) {
      if (calendar.get(Calendar.YEAR) - dot > MAX_DEEP) {
        throw new IllegalArgumentException("Invalid cron expression \"" + this.expression
            + "\" led to runaway search for next trigger");
      }
      doNext(calendar, dot);
    }
  }

  private int findNextDay(Calendar calendar, BitSet daysOfMonth, int dayOfMonth, BitSet daysOfWeek,
      int dayOfWeek, List<Integer> resets) {

    int count = 0;
    int max = 366;
    // the DAY_OF_WEEK values in java.util.Calendar start with 1 (Sunday),
    // but in the cron pattern, they start with 0, so we subtract 1 here
    while ((!daysOfMonth.get(dayOfMonth) || !daysOfWeek.get(dayOfWeek - 1)) && count++ < max) {
      calendar.add(Calendar.DAY_OF_MONTH, 1);
      dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
      dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
      reset(calendar, resets);
    }
    if (count >= max) {
      throw new IllegalArgumentException(
          "Overflow in day for expression \"" + this.expression + "\"");
    }
    return dayOfMonth;
  }

  private int findNext(BitSet bits, int value, Calendar calendar, int field, int nextField,
      List<Integer> lowerOrders) {
    int nextValue = bits.nextSetBit(value);
    // roll over if needed
    if (nextValue == -1) {
      calendar.add(nextField, 1);
      reset(calendar, Collections.singletonList(field));
      nextValue = bits.nextSetBit(0);
    }
    if (nextValue != value) {
      calendar.set(field, nextValue);
      reset(calendar, lowerOrders);
    }
    return nextValue;
  }

  private void reset(Calendar calendar, List<Integer> fields) {
    for (int field : fields) {
      calendar.set(field, field == Calendar.DAY_OF_MONTH ? 1 : 0);
    }
  }

  private void parse() {
    String[] fields = this.expression.split("\\s+");
    if (fields.length != CRON_FIELDS) {
      throw Exceptions.fail("expression should have 6 fields");
    }
    setNumberBit(seconds, fields[0], 60);
    setNumberBit(minutes, fields[1], 60);
    setNumberBit(hours, fields[2], 24);
    setDaysOfMonth(fields[3]);
    setMonths(fields[4]);
    setDaysOfWeek(fields[5]);
  }

  private void setDaysOfWeek(String field) {
    for (Map.Entry<String, String> entry : WEEK_MAP.entrySet()) {
      field = field.replace(entry.getKey(), entry.getValue());
    }
    setNumberBit(daysOfWeek, field, 7);
  }

  private void setMonths(String field) {
    for (Map.Entry<String, String> entry : MONTH_MAP.entrySet()) {
      field = field.replace(entry.getKey(), entry.getValue());
    }
    setNumberBit(months, field, 13);
    for (int i = 1; i <= MONTH_MAP.size(); i++) {
      months.set(i - 1, months.get(i));
    }
  }

  private void setDaysOfMonth(String field) {
    setNumberBit(daysOfMonth, field, 32);
    this.daysOfMonth.clear(0);
  }

  private void setNumberBit(BitSet bit, String field, int size) {
    for (String line : field.split(Strings.COMMA)) {
      if (Strings.ANY.equals(line.trim()) || Strings.QUESTION_MARK.equals(line.trim())) {
        bit.set(0, size);
      } else if (line.contains(Strings.SLASH)) {
        parseDuration(bit, line, size);
      } else if (line.contains(Strings.DASH)) {
        String[] fromTo = line.split(Strings.DASH);
        if (fromTo.length != 2) {
          throw Exceptions.fail(errorMessage());
        }
        bit.set(Integer.parseInt(fromTo[0].trim()), Integer.parseInt(fromTo[1].trim()) + 1);
      } else {
        bit.set(Integer.parseInt(line.trim()));
      }
    }
  }

  private void parseDuration(BitSet bit, String line, int size) {
    String[] arr = line.split(Strings.SLASH);
    if (arr.length != 2) {
      throw Exceptions.fail(errorMessage());
    }
    int step = Integer.parseInt(arr[1].trim());
    if (step < 1) {
      throw Exceptions.fail(errorMessage());
    }
    int from;
    int to = size - 1;
    if (arr[0].contains(Strings.DASH)) {
      String[] fromTo = arr[0].split(Strings.DASH);
      if (fromTo.length != 2) {
        throw Exceptions.fail(errorMessage());
      }
      from = Integer.parseInt(fromTo[0].trim());
      to = Integer.parseInt(fromTo[1].trim());
    } else {
      from = Integer.parseInt(arr[0].trim());
    }
    for (int i = from; i <= to; i += step) {
      bit.set(i);
    }
  }

  private String errorMessage() {
    return String.format("illegal expression '%s'", this.expression);
  }

}
