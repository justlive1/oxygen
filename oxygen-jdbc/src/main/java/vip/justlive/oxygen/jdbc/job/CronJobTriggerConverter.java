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
package vip.justlive.oxygen.jdbc.job;

import java.util.TimeZone;
import vip.justlive.oxygen.core.job.CronJobTrigger;
import vip.justlive.oxygen.core.job.JobTrigger;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * cron实现的trigger转换器
 *
 * @author wubo
 */
public class CronJobTriggerConverter implements Converter {

  @Override
  public int type() {
    return 2;
  }

  @Override
  public Class<? extends JobTrigger> classType() {
    return CronJobTrigger.class;
  }

  @Override
  public JobTriggerEntity convert(JobTrigger trigger) {
    if (trigger instanceof CronJobTrigger) {
      CronJobTrigger jobTrigger = (CronJobTrigger) trigger;
      JobTriggerEntity entity = new JobTriggerEntity().setTriggerType(type())
          .setTriggerValue(jobTrigger.getCron() + Strings.SEMICOLON + jobTrigger.getTimeZoneId());
      Utils.fillEntityProperty(entity, jobTrigger);
      return entity;
    }
    return null;
  }

  @Override
  public JobTrigger convert(JobTriggerEntity entity) {
    if (entity.getTriggerType() != null && entity.getTriggerType() == type()) {
      String[] arr = entity.getTriggerValue().split(Strings.SEMICOLON);
      CronJobTrigger trigger = new CronJobTrigger(entity.getTriggerKey(), entity.getJobKey(),
          arr[0], TimeZone.getTimeZone(arr[1]));
      Utils.fillTriggerProperty(trigger, entity);
      return trigger;
    }
    return null;
  }
}
