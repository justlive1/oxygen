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

import vip.justlive.oxygen.core.job.DelayOrRateJobTrigger;
import vip.justlive.oxygen.core.job.JobTrigger;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * 固定延迟或周期trigger转换
 *
 * @author wubo
 */
public class DelayOrRateJobTriggerConverter implements Converter {

  @Override
  public int type() {
    return 1;
  }

  @Override
  public Class<? extends JobTrigger> classType() {
    return DelayOrRateJobTrigger.class;
  }

  @Override
  public JobTriggerEntity convert(JobTrigger trigger) {
    if (trigger instanceof DelayOrRateJobTrigger) {
      DelayOrRateJobTrigger jobTrigger = (DelayOrRateJobTrigger) trigger;
      return new JobTriggerEntity().setJobKey(trigger.getJobKey()).setTriggerKey(trigger.getKey())
          .setTriggerType(type()).setStartTime(jobTrigger.getEndTime())
          .setTriggerValue(
              jobTrigger.getInitialDelay() + Strings.SEMICOLON + jobTrigger.getFixedOffset()
                  + Strings.SEMICOLON + jobTrigger.isDelay())
          .setEndTime(jobTrigger.getEndTime())
          .setPreviousFireTime(jobTrigger.getPreviousFireTime())
          .setNextFireTime(jobTrigger.getNextFireTime())
          .setLastCompletedTime(jobTrigger.getLastCompletedTime());
    }
    return null;
  }

  @Override
  public JobTrigger convert(JobTriggerEntity entity) {
    if (entity.getTriggerType() != null && entity.getTriggerType() == type()) {
      String[] arr = entity.getTriggerValue().split(Strings.SEMICOLON);
      DelayOrRateJobTrigger trigger = new DelayOrRateJobTrigger(entity.getTriggerKey(),
          entity.getJobKey(), Long.parseLong(arr[0]), Long.parseLong(arr[1]),
          Boolean.parseBoolean(arr[2]));
      trigger.setStartTime(entity.getStartTime());
      trigger.setEndTime(entity.getEndTime());
      trigger.setNextFireTime(entity.getNextFireTime());
      trigger.setPreviousFireTime(entity.getPreviousFireTime());
      trigger.setLastCompletedTime(entity.getLastCompletedTime());
      return trigger;
    }
    return null;
  }
}
