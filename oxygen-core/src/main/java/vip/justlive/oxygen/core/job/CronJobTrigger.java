/*
 * Copyright (C) 2021 the original author or authors.
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
package vip.justlive.oxygen.core.job;

import java.util.Date;
import java.util.TimeZone;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.timer.CronExpression;

/**
 * cron实现的trigger
 *
 * @author wubo
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class CronJobTrigger extends CoreJobTrigger {

  private final String cron;
  private final CronExpression expression;

  public CronJobTrigger(String jobKey, String cron) {
    this(jobKey, jobKey, cron, TimeZone.getDefault());
  }

  public CronJobTrigger(String key, String jobKey, String cron) {
    this(key, jobKey, cron, TimeZone.getDefault());
  }

  public CronJobTrigger(String key, String jobKey, String cron, TimeZone timeZone) {
    super(MoreObjects.notNull(key), MoreObjects.notNull(jobKey));
    this.cron = MoreObjects.notNull(cron);
    this.expression = new CronExpression(cron, timeZone);
  }

  @Override
  public Long getFireTimeAfter(long timestamp) {
    Date date = expression.next(new Date(timestamp));
    while (date != null && (startTime != null && startTime > date.getTime())
        && (endTime != null && endTime > date.getTime())) {
      date = expression.next(date);
    }
    if (date != null) {
      return date.getTime();
    }
    return null;
  }

}
