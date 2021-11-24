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

import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * jobTrigger基础类
 *
 * @author wubo
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public abstract class CoreJobTrigger implements JobTrigger {
  
  protected AtomicLong rounds = new AtomicLong(0);
  
  protected final String key;
  protected final String jobKey;
  
  protected Long startTime;
  protected Long endTime;
  
  protected Long previousFireTime;
  protected Long nextFireTime;
  protected Long lastCompletedTime;
  
  protected Integer state;
  
  @Override
  public void setLastCompletedTime(Long lastCompletedTime) {
    this.lastCompletedTime = lastCompletedTime;
    this.rounds.incrementAndGet();
  }
  
  @Override
  public Long triggerFired(long timestamp) {
    previousFireTime = nextFireTime;
    Long next = timestamp;
    if (nextFireTime != null) {
      next = nextFireTime;
    }
    while (next != null && next <= timestamp) {
      next = getFireTimeAfter(next);
    }
    nextFireTime = next;
    return nextFireTime;
  }
}
