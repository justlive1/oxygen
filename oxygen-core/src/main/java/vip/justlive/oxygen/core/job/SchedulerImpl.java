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
import java.util.concurrent.atomic.AtomicLong;
import vip.justlive.oxygen.core.job.JobResource.WaitingTaskFuture;
import vip.justlive.oxygen.core.util.concurrent.RepeatRunnable;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * 调度实现类
 *
 * @author wubo
 */
public class SchedulerImpl implements Scheduler {

  private final JobResource resource;
  private final SchedulerRunnable schedulerRunnable;
  private final RepeatRunnable schedulerThread;
  private final AtomicLong ids = new AtomicLong();

  public SchedulerImpl(JobResource resource) {
    this.resource = resource;
    this.schedulerRunnable = new SchedulerRunnable(resource);
    this.schedulerThread = new RepeatRunnable("Job_Scheduler", schedulerRunnable);
    this.resource.setSchedulerThread(schedulerThread);
    Signaler signaler = new Signaler() {
      @Override
      public void schedulingChange() {
        schedulerRunnable.schedulingChange();
      }

      @Override
      public synchronized void triggerCompleted(String triggerKey, long previousFireTime) {
        List<WaitingTaskFuture> list = resource.futures.get(triggerKey);
        if (list == null) {
          return;
        }
        WaitingTaskFuture future = null;
        for (WaitingTaskFuture ft : list) {
          if (ft.nextFireTime == previousFireTime) {
            future = ft;
            break;
          }
        }
        if (future != null) {
          list.remove(future);
        }
      }
    };
    this.resource.setSignaler(signaler);
    this.resource.getJobStore().initialize(this.resource.getSignaler());
  }


  @Override
  public void start() {
    ThreadUtils.residentPool().add(schedulerThread);
    schedulerThread.awaitRunning();
    resource.getSchedulerPlugins().forEach(SchedulerPlugin::start);
  }

  @Override
  public void shutdown() {
    resource.getSchedulerPlugins().forEach(SchedulerPlugin::shutdown);
    schedulerRunnable.shutdown();
    schedulerThread.shutdown();
  }

  @Override
  public boolean isShutdown() {
    return schedulerThread.isShutdown();
  }

  @Override
  public void addJob(JobInfo jobInfo, boolean replaceExisting) {
    resource.getJobStore().storeJob(jobInfo, replaceExisting);
  }

  @Override
  public void scheduleJob(JobInfo jobInfo, JobTrigger trigger) {
    Long nextFireTime = trigger.computeNextFireTime();
    if (nextFireTime == null) {
      throw new IllegalArgumentException(
          "job will never fire with triggerKey '" + trigger.getKey() + "'");
    }
    resource.getJobStore().storeJob(jobInfo, true);
    resource.getJobStore().storeTrigger(trigger, true);
    resource.getSignaler().schedulingChange();
  }

  @Override
  public void scheduleJob(JobTrigger trigger) {
    Long nextFireTime = trigger.computeNextFireTime();
    if (nextFireTime == null) {
      throw new IllegalArgumentException(
          "job will never fire with triggerKey '" + trigger.getKey() + "'");
    }
    resource.getJobStore().storeTrigger(trigger, false);
    resource.getSignaler().schedulingChange();
  }

  @Override
  public void removeJob(String jobKey) {
    removeJobWaitingTask(jobKey);
    resource.getJobStore().removeJob(jobKey);
  }

  @Override
  public void triggerJob(String jobKey) {
    FixedTimeJobTrigger trigger = new FixedTimeJobTrigger("Tmp_Trigger_" + ids.getAndIncrement(),
        jobKey);
    long now = System.currentTimeMillis();
    trigger.setStartTime(now);
    trigger.computeNextFireTime();
    resource.getJobStore().storeTrigger(trigger, false);
    resource.getSignaler().schedulingChange();
  }

  @Override
  public void pauseJob(String jobKey) {
    resource.getJobStore().pauseJob(jobKey);
    removeJobWaitingTask(jobKey);
  }

  @Override
  public void pauseTrigger(String triggerKey) {
    resource.getJobStore().pauseTrigger(triggerKey);
    removeWaitingTask(triggerKey);
  }

  @Override
  public void resumeJob(String jobKey) {
    resource.getJobStore().resumeJob(jobKey);
    resource.getSignaler().schedulingChange();
  }

  @Override
  public void resumeTrigger(String triggerKey) {
    resource.getJobStore().resumeTrigger(triggerKey);
    resource.getSignaler().schedulingChange();
  }

  private void removeWaitingTask(String triggerKey) {
    List<WaitingTaskFuture> list = resource.futures.remove(triggerKey);
    if (list == null) {
      return;
    }
    for (WaitingTaskFuture future : list) {
      future.future.cancel(true);
    }
    list.clear();
  }

  private void removeJobWaitingTask(String jobKey) {
    List<JobTrigger> list = resource.getJobStore().getJobTrigger(jobKey);
    if (list == null) {
      return;
    }
    for (JobTrigger jobTrigger : list) {
      removeWaitingTask(jobTrigger.getKey());
    }

  }
}
