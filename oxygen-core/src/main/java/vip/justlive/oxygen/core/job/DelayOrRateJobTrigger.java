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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 固定延迟或周期trigger
 *
 * @author wubo
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DelayOrRateJobTrigger extends CoreJobTrigger {
  
  private final long initialDelay;
  private final long fixedOffset;
  private final boolean delay;
  
  public DelayOrRateJobTrigger(String jobKey, long fixedOffset, boolean delay) {
    this(jobKey, fixedOffset, fixedOffset, delay);
  }
  
  public DelayOrRateJobTrigger(String jobKey, long initialDelay, long fixedOffset, boolean delay) {
    this(jobKey, jobKey, initialDelay, fixedOffset, delay);
  }
  
  public DelayOrRateJobTrigger(String key, String jobKey, long initialDelay, long fixedOffset,
      boolean delay) {
    super(key, jobKey);
    this.initialDelay = initialDelay;
    this.fixedOffset = fixedOffset;
    this.delay = delay;
  }
  
  @Override
  public Long getFireTimeAfter(long timestamp) {
    long offset = fixedOffset;
    if (lastCompletedTime == null) {
      offset = initialDelay;
    }
    long time = timestamp + offset;
    while (startTime != null && startTime > time) {
      time += offset;
    }
    if (endTime != null && time > endTime) {
      return null;
    }
    return time;
  }
  
  @Override
  public void setLastCompletedTime(Long lastCompletedTime) {
    super.setLastCompletedTime(lastCompletedTime);
    if (lastCompletedTime != null && isDelay()) {
      nextFireTime = getFireTimeAfter(lastCompletedTime);
    }
  }
  
  @Override
  public Long triggerFired(long timestamp) {
    if (isDelay() && lastCompletedTime != null && lastCompletedTime < nextFireTime) {
      return Long.MAX_VALUE;
    }
    if (!isDelay() && nextFireTime != null) {
      timestamp = nextFireTime;
    }
    return super.triggerFired(timestamp);
  }
  
  @Override
  public Long computeNextFireTime() {
    if (!isDelay() && nextFireTime != null) {
      return super.triggerFired(nextFireTime);
    }
    return super.computeNextFireTime();
  }
}
