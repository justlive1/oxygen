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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * job执行任务
 *
 * @author wubo
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class JobRunTask implements Runnable {

  public static final int NOOP = 0;
  public static final int DELETE = 1;

  private final Job job;
  private final JobContext ctx;
  private final JobTrigger trigger;
  private final JobResource resource;

  @Override
  public void run() {
    long startTime = System.currentTimeMillis();
    try {
      if (log.isDebugEnabled()) {
        log.debug("calling execute on job {}", ctx.getJobInfo().getKey());
      }
      job.execute(ctx);
    } catch (Exception e) {
      log.info("job {} throw exception", ctx.getJobInfo().getKey(), e);
    }

    long lastCompletedTime = System.currentTimeMillis();
    if (log.isDebugEnabled()) {
      log.debug("job {} elapsed {}ms", ctx.getJobInfo().getKey(), lastCompletedTime - startTime);
    }

    int state = NOOP;
    if (trigger.getNextFireTime() == null) {
      state = DELETE;
    }

    resource.getJobStore().triggerCompleted(trigger, state);
    resource.getSignaler().triggerCompleted(trigger.getKey(), ctx.getExpectedFireTime());
  }
}
