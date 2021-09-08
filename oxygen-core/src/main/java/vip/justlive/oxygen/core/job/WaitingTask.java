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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 等待任务
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class WaitingTask implements Runnable {

  private final Job job;
  private final JobContext ctx;
  private final JobResource resource;
  private final JobTrigger jobTrigger;

  @Override
  public void run() {
    if (resource.getSchedulerThread() == null || resource.getSchedulerThread().isShutdown()) {
      return;
    }

    TriggerFiredResult result = resource.getJobStore().triggerFired(jobTrigger);
    if (result == null) {
      resource.getJobStore().releaseTrigger(jobTrigger);
      return;
    }

    if (result.getException() != null) {
      log.error("firing trigger error {}", jobTrigger, result.getException());
      resource.getJobStore().releaseTrigger(jobTrigger);
      return;
    }

    resource.getPool()
        .execute(jobTrigger.getJobKey(), new JobRunTask(job, ctx, jobTrigger, resource));
  }
}
