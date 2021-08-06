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

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * map实现的job store
 *
 * @author wubo
 */
public class MapJobStore implements JobStore {

  private final Map<String, JobInfoWrapper> jobInfos = new HashMap<>(4);
  private final Map<String, JobTriggerWrapper> triggerMap = new HashMap<>(4);
  private final TreeSet<JobTriggerWrapper> timeTriggers = new TreeSet<>(new JobTriggerComparator());
  private Signaler signaler;

  @Override
  public void initialize(Signaler signaler) {
    this.signaler = signaler;
  }

  @Override
  public synchronized void storeJob(JobInfo jobInfo, boolean replaceExisting) {
    JobInfoWrapper wrapper = jobInfos.get(jobInfo.getKey());
    if (wrapper == null) {
      jobInfos.put(jobInfo.getKey(), new JobInfoWrapper(jobInfo));
    } else if (replaceExisting) {
      wrapper.jobInfo = jobInfo;
    }
  }

  @Override
  public JobInfo getJobInfo(String jobKey) {
    JobInfoWrapper wrapper = jobInfos.get(jobKey);
    if (wrapper != null) {
      return wrapper.jobInfo;
    }
    return null;
  }

  @Override
  public synchronized void removeJob(String jobKey) {
    JobInfoWrapper wrapper = jobInfos.remove(jobKey);
    if (wrapper != null) {
      wrapper.triggers.forEach(k -> triggerMap.remove(k.trigger.getKey()));
    }
  }

  @Override
  public synchronized void storeTrigger(JobTrigger trigger) {
    if (trigger == null) {
      return;
    }
    JobTriggerWrapper triggerWrapper = new JobTriggerWrapper(trigger);
    if (triggerMap.putIfAbsent(trigger.getKey(), triggerWrapper) != null) {
      throw new IllegalArgumentException("trigger key '" + trigger.getKey() + "' already exists");
    }
    JobInfoWrapper wrapper = jobInfos.get(trigger.getJobKey());
    if (wrapper != null) {
      wrapper.triggers.add(triggerWrapper);
    }
    timeTriggers.add(triggerWrapper);
  }

  @Override
  public synchronized List<JobTrigger> getJobTrigger(String jobKey) {
    List<JobTrigger> list = new LinkedList<>();
    JobInfoWrapper wrapper = jobInfos.get(jobKey);
    if (wrapper != null) {
      wrapper.triggers.forEach(item -> list.add(item.trigger));
    }
    return list;
  }

  @Override
  public synchronized void removeTrigger(String triggerKey) {
    JobTriggerWrapper trigger = triggerMap.remove(triggerKey);
    if (trigger == null) {
      return;
    }
    JobInfoWrapper wrapper = jobInfos.get(trigger.trigger.getJobKey());
    if (wrapper == null) {
      return;
    }
    wrapper.triggers.remove(trigger);
    timeTriggers.remove(trigger);
  }

  @Override
  public synchronized void pauseJob(String jobKey) {
    JobInfoWrapper wrapper = jobInfos.get(jobKey);
    if (wrapper == null) {
      return;
    }
    for (JobTriggerWrapper trigger : wrapper.triggers) {
      pauseTrigger(trigger.trigger.getKey());
    }
  }

  @Override
  public synchronized void pauseTrigger(String triggerKey) {
    JobTriggerWrapper wrapper = triggerMap.get(triggerKey);
    if (wrapper == null) {
      return;
    }
    wrapper.state = STATE_PAUSED;
    timeTriggers.remove(wrapper);
  }

  @Override
  public synchronized void resumeJob(String jobKey) {
    JobInfoWrapper wrapper = jobInfos.get(jobKey);
    if (wrapper == null) {
      return;
    }
    for (JobTriggerWrapper trigger : wrapper.triggers) {
      resumeTrigger(trigger.trigger.getKey());
    }
  }

  @Override
  public synchronized void resumeTrigger(String triggerKey) {
    JobTriggerWrapper wrapper = triggerMap.get(triggerKey);
    if (wrapper == null || wrapper.state != STATE_PAUSED) {
      return;
    }
    wrapper.state = STATE_WAITING;
    Long nextFireTime = wrapper.trigger.computeNextFireTime(System.currentTimeMillis());
    if (nextFireTime != null) {
      timeTriggers.add(wrapper);
    } else {
      wrapper.state = STATE_COMPLETE;
    }
  }

  @Override
  public synchronized List<JobTrigger> acquireNextTriggers(long maxTimestamp, int maxSize) {
    List<JobTrigger> list = new LinkedList<>();
    while (list.size() < maxSize && !timeTriggers.isEmpty()) {
      JobTriggerWrapper wrapper = timeTriggers.pollFirst();
      if (wrapper != null && wrapper.trigger.getNextFireTime() != null) {
        if (wrapper.trigger.getNextFireTime() > maxTimestamp) {
          timeTriggers.add(wrapper);
          break;
        }
        list.add(wrapper.trigger);
        wrapper.state = STATE_ACQUIRED;
      }
    }
    return list;
  }

  @Override
  public synchronized void releaseTrigger(JobTrigger trigger) {
    JobTriggerWrapper wrapper = triggerMap.get(trigger.getKey());
    if (wrapper == null) {
      return;
    }
    if (wrapper.state == STATE_ACQUIRED) {
      wrapper.state = STATE_WAITING;
      timeTriggers.add(wrapper);
    }
  }

  @Override
  public synchronized TriggerFiredResult triggerFired(JobTrigger trigger) {
    JobTriggerWrapper wrapper = triggerMap.get(trigger.getKey());
    if (wrapper == null || wrapper.state != STATE_ACQUIRED) {
      return null;
    }

    timeTriggers.remove(wrapper);

    wrapper.trigger.computeNextFireTime(System.currentTimeMillis());
    wrapper.state = STATE_WAITING;
    if (wrapper.trigger.getNextFireTime() != null) {
      timeTriggers.add(wrapper);
    }
    return new TriggerFiredResult(trigger, null);
  }

  @Override
  public synchronized void triggerCompleted(JobTrigger trigger, int state) {
    JobTriggerWrapper wrapper = triggerMap.get(trigger.getKey());
    if (wrapper == null) {
      return;
    }
    if (state == JobRunTask.DELETE) {
      removeTrigger(trigger.getKey());
    }

    timeTriggers.remove(wrapper);

    long lastCompletedTime = System.currentTimeMillis();
    trigger.setLastCompletedTime(lastCompletedTime);
    wrapper.trigger.setLastCompletedTime(lastCompletedTime);

    if (wrapper.trigger.getNextFireTime() != null) {
      timeTriggers.add(wrapper);
    }
    signaler.schedulingChange();
  }

  static class JobInfoWrapper {

    JobInfo jobInfo;
    final List<JobTriggerWrapper> triggers = new LinkedList<>();

    JobInfoWrapper(JobInfo jobInfo) {
      this.jobInfo = jobInfo;
    }
  }

  @ToString
  @EqualsAndHashCode
  @RequiredArgsConstructor
  static class JobTriggerWrapper {

    final JobTrigger trigger;
    int state;
  }

  static final int STATE_WAITING = 0;
  static final int STATE_ACQUIRED = 1;
  static final int STATE_COMPLETE = 2;
  static final int STATE_PAUSED = 3;

  static class JobTriggerComparator implements Comparator<JobTriggerWrapper> {

    @Override
    public int compare(JobTriggerWrapper o1, JobTriggerWrapper o2) {
      Long nextFireTime1 = o1.trigger.getNextFireTime();
      Long nextFireTime2 = o2.trigger.getNextFireTime();
      if (nextFireTime1 != null || nextFireTime2 != null) {
        if (nextFireTime1 == null) {
          return 1;
        }
        if (nextFireTime2 == null) {
          return -1;
        }
        if (nextFireTime1 < nextFireTime2) {
          return -1;
        } else if (nextFireTime1 > nextFireTime2) {
          return 1;
        }
      }
      return o1.trigger.getKey().compareTo(o2.trigger.getKey());
    }

  }
}
