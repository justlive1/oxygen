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

import vip.justlive.oxygen.core.job.FixedTimeJobTrigger;
import vip.justlive.oxygen.core.job.JobTrigger;

/**
 * 固定触发器转换类
 *
 * @author wubo
 */
public class FixedTimeJobTriggerConverter implements Converter {

  @Override
  public int type() {
    return 0;
  }

  @Override
  public Class<? extends JobTrigger> classType() {
    return FixedTimeJobTrigger.class;
  }

  @Override
  public JobTriggerEntity convert(JobTrigger trigger) {
    if (trigger instanceof FixedTimeJobTrigger) {
      FixedTimeJobTrigger jobTrigger = (FixedTimeJobTrigger) trigger;
      return new JobTriggerEntity().setJobKey(trigger.getJobKey()).setTriggerKey(trigger.getKey())
          .setTriggerType(type()).setStartTime(jobTrigger.getEndTime())
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
      FixedTimeJobTrigger trigger = new FixedTimeJobTrigger(entity.getTriggerKey(),
          entity.getJobKey());
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