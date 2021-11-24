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

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.util.concurrent.RepeatRunnable;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * 丢失状态的job处理
 *
 * @author wubo
 */
@Slf4j
public class LostSchedulerPlugin implements SchedulerPlugin {
  
  private final RepeatRunnable repeatRunnable = new RepeatRunnable("Job_Lost",
      this::recoverLostJobs);
  
  private JobResource resource;
  
  @Override
  public void initialize(JobResource resource, Scheduler scheduler) {
    this.resource = resource;
  }
  
  @Override
  public void start() {
    ThreadUtils.residentPool().add(repeatRunnable);
    repeatRunnable.awaitRunning();
  }
  
  @Override
  public void shutdown() {
    repeatRunnable.shutdown();
  }
  
  private void recoverLostJobs() {
    long startTime = System.currentTimeMillis();
    int count = 0;
    try {
      if (log.isDebugEnabled()) {
        log.debug("scanning for lost...");
      }
      
      long maxTimestamp = System.currentTimeMillis() - resource.getConf().getLostThreshold();
      
      List<JobTrigger> triggers = resource.getJobStore()
          .acquireTriggersInState(maxTimestamp, JobConstants.STATE_ACQUIRED);
      if (triggers == null || triggers.isEmpty()) {
        return;
      }
      
      for (JobTrigger trigger : triggers) {
        if (doUpdateOfLostTrigger(trigger)) {
          count++;
        }
      }
      
    } catch (Exception e) {
      log.error("Error handling lost", e);
    } finally {
      
      if (log.isDebugEnabled()) {
        log.debug("handle lost end and restart {} trigger(s)", count);
      }
      
      long timeToSleep =
          resource.getConf().getLostThreshold() - (System.currentTimeMillis() - startTime);
      ThreadUtils.sleep(timeToSleep);
    }
  }
  
  private boolean doUpdateOfLostTrigger(JobTrigger trigger) {
    trigger.computeNextFireTime();
    
    if (trigger.getNextFireTime() == null) {
      resource.getJobStore().storeTrigger(trigger, JobConstants.STATE_COMPLETE, true);
    } else {
      resource.getJobStore().storeTrigger(trigger, true);
      return true;
    }
    
    return false;
  }
}
