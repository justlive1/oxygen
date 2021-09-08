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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.job.JobResource.WaitingTaskFuture;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * 调度处理类
 *
 * @author wubo
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class SchedulerRunnable implements Runnable {

  private final JobResource resource;

  private final AtomicLong version = new AtomicLong();
  private final Random random = new Random(System.currentTimeMillis());
  private final Object lock = new Object();
  private boolean shutdown;

  @Override
  public void run() {
    if (shutdown) {
      return;
    }
    try {
      long currentVersion = this.version.get();
      List<JobTrigger> triggers = fetch();

      if (log.isDebugEnabled()) {
        log.debug("fetch triggers {}", triggers);
      }

      if (triggers != null) {
        for (JobTrigger trigger : triggers) {
          addWaitingTask(trigger);
        }
      }

      idleWait(currentVersion);
    } catch (Exception e) {
      log.error("some error occurred", e);
    }
  }

  public boolean isScheduleChanged(long currentVersion) {
    return this.version.get() != currentVersion;
  }

  public void schedulingChange() {
    version.incrementAndGet();
    synchronized (lock) {
      lock.notifyAll();
    }
  }

  private List<JobTrigger> fetch() {
    try {
      return resource.getJobStore()
          .acquireNextTriggers(System.currentTimeMillis() + resource.getConf().getIdleWaitTime(),
              resource.getConf().getFetchMaxSize());
    } catch (Exception e) {
      log.error("acquireNextTriggers failed.", e);
    }
    return Collections.emptyList();
  }

  private void addWaitingTask(JobTrigger trigger) {
    JobInfo jobInfo = resource.getJobStore().getJobInfo(trigger.getJobKey());
    if (jobInfo == null) {
      return;
    }

    List<WaitingTaskFuture> list = resource.futures
        .computeIfAbsent(trigger.getKey(), k -> new LinkedList<>());

    for (WaitingTaskFuture future : list) {
      if (future.nextFireTime == trigger.getNextFireTime()) {
        return;
      }
    }

    Job job;
    try {
      Class<?> clazz = ClassUtils.forName(jobInfo.getHandlerClass());
      job = (Job) ClassUtils.newInstance(clazz);
    } catch (Exception e) {
      log.error("new job instance error", e);
      resource.getJobStore().releaseTrigger(trigger);
      return;
    }
    if (log.isDebugEnabled()) {
      log.debug("add job {} to waiting task [{}ms]", job,
          trigger.getNextFireTime() - System.currentTimeMillis());
    }
    JobContext ctx = new JobContextImpl(jobInfo, trigger.getNextFireTime());
    ScheduledFuture<Void> future = ThreadUtils.globalTimer()
        .schedule(new WaitingTask(job, ctx, resource, trigger),
            trigger.getNextFireTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    WaitingTaskFuture waitingTaskFuture = new WaitingTaskFuture();
    waitingTaskFuture.nextFireTime = trigger.getNextFireTime();
    waitingTaskFuture.future = future;
    list.add(waitingTaskFuture);
  }

  private void idleWait(long currentVersion) {
    long waitTime = resource.getConf().getIdleWaitTime() - random
        .nextInt(resource.getConf().getIdleWaitRandom());
    synchronized (lock) {
      try {
        if (!isScheduleChanged(currentVersion)) {
          lock.wait(waitTime);
        }
      } catch (InterruptedException ignore) {
        Thread.currentThread().interrupt();
      }
    }
  }

  void shutdown() {
    shutdown = true;
    synchronized (lock) {
      lock.notifyAll();
    }
  }

}
