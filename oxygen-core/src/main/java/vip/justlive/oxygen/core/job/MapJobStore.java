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

/**
 * map实现的job store
 *
 * @author wubo
 */
public class MapJobStore implements JobStore {
  
  private final Map<String, JobInfoWrapper> jobInfos = new HashMap<>(4);
  private final Map<String, JobTrigger> triggerMap = new HashMap<>(4);
  private final TreeSet<JobTrigger> timeTriggers = new TreeSet<>(new JobTriggerComparator());
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
    } else {
      throw new IllegalArgumentException("job '" + jobInfo.getKey() + "' already exists");
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
      wrapper.triggers.forEach(k -> triggerMap.remove(k.getKey()));
    }
  }
  
  @Override
  public synchronized void storeTrigger(JobTrigger trigger, int state, boolean replaceExisting) {
    if (trigger == null) {
      return;
    }
    
    trigger.setState(state);
    
    JobTrigger existedTrigger = triggerMap.putIfAbsent(trigger.getKey(), trigger);
    
    if (!replaceExisting && existedTrigger != null) {
      throw new IllegalArgumentException("trigger key '" + trigger.getKey() + "' already exists");
    }
    JobInfoWrapper wrapper = jobInfos.get(trigger.getJobKey());
    if (wrapper == null) {
      throw new IllegalArgumentException(
          "the job '" + trigger.getJobKey() + "' referenced by the trigger does not exist.");
    }
    if (existedTrigger != null) {
      wrapper.triggers.remove(existedTrigger);
      timeTriggers.remove(existedTrigger);
    }
    wrapper.triggers.add(trigger);
    timeTriggers.add(trigger);
  }
  
  @Override
  public synchronized List<JobTrigger> getJobTrigger(String jobKey) {
    List<JobTrigger> list = new LinkedList<>();
    JobInfoWrapper wrapper = jobInfos.get(jobKey);
    if (wrapper != null) {
      list.addAll(wrapper.triggers);
    }
    return list;
  }
  
  @Override
  public synchronized void removeTrigger(String triggerKey) {
    JobTrigger trigger = triggerMap.remove(triggerKey);
    if (trigger == null) {
      return;
    }
    JobInfoWrapper wrapper = jobInfos.get(trigger.getJobKey());
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
    for (JobTrigger trigger : wrapper.triggers) {
      pauseTrigger(trigger.getKey());
    }
  }
  
  @Override
  public synchronized void pauseTrigger(String triggerKey) {
    JobTrigger trigger = triggerMap.get(triggerKey);
    if (trigger == null) {
      return;
    }
    trigger.setState(JobConstants.STATE_PAUSED);
    timeTriggers.remove(trigger);
  }
  
  @Override
  public synchronized void resumeJob(String jobKey) {
    JobInfoWrapper wrapper = jobInfos.get(jobKey);
    if (wrapper == null) {
      return;
    }
    for (JobTrigger trigger : wrapper.triggers) {
      resumeTrigger(trigger.getKey());
    }
  }
  
  @Override
  public synchronized void resumeTrigger(String triggerKey) {
    JobTrigger trigger = triggerMap.get(triggerKey);
    if (trigger == null || trigger.getState() != JobConstants.STATE_PAUSED) {
      return;
    }
    trigger.setState(JobConstants.STATE_WAITING);
    Long nextFireTime = trigger.computeNextFireTime();
    if (nextFireTime != null) {
      timeTriggers.add(trigger);
    } else {
      trigger.setState(JobConstants.STATE_COMPLETE);
    }
  }
  
  @Override
  public synchronized List<JobTrigger> acquireNextTriggers(long maxTimestamp, int maxSize) {
    List<JobTrigger> list = new LinkedList<>();
    while (list.size() < maxSize && !timeTriggers.isEmpty()) {
      JobTrigger trigger = timeTriggers.pollFirst();
      if (trigger != null && trigger.getNextFireTime() != null) {
        if (trigger.getNextFireTime() > maxTimestamp) {
          timeTriggers.add(trigger);
          break;
        }
        list.add(trigger);
        trigger.setState(JobConstants.STATE_ACQUIRED);
      }
    }
    return list;
  }
  
  @Override
  public synchronized void releaseTrigger(JobTrigger trigger) {
    JobTrigger jobTrigger = triggerMap.get(trigger.getKey());
    if (jobTrigger == null) {
      return;
    }
    if (jobTrigger.getState() == JobConstants.STATE_ACQUIRED) {
      jobTrigger.setState(JobConstants.STATE_WAITING);
      timeTriggers.add(jobTrigger);
    }
  }
  
  @Override
  public synchronized TriggerFiredResult triggerFired(JobTrigger trigger) {
    JobTrigger jobTrigger = triggerMap.get(trigger.getKey());
    if (jobTrigger == null || jobTrigger.getState() != JobConstants.STATE_ACQUIRED) {
      return null;
    }
    
    timeTriggers.remove(jobTrigger);
    
    jobTrigger.triggerFired(System.currentTimeMillis());
    jobTrigger.setState(JobConstants.STATE_WAITING);
    if (jobTrigger.getNextFireTime() != null) {
      timeTriggers.add(jobTrigger);
    }
    return new TriggerFiredResult(trigger, null);
  }
  
  @Override
  public synchronized void triggerCompleted(JobTrigger trigger, int state) {
    JobTrigger jobTrigger = triggerMap.get(trigger.getKey());
    if (jobTrigger == null) {
      return;
    }
    if (state == JobRunTask.DELETE) {
      removeTrigger(trigger.getKey());
    }
    
    timeTriggers.remove(jobTrigger);
    
    long lastCompletedTime = System.currentTimeMillis();
    
    jobTrigger.setLastCompletedTime(lastCompletedTime);
    if (jobTrigger != trigger) {
      trigger.setLastCompletedTime(lastCompletedTime);
    }
    
    if (jobTrigger.getNextFireTime() != null) {
      jobTrigger.setState(JobConstants.STATE_ACQUIRED);
      timeTriggers.add(jobTrigger);
    }
    signaler.schedulingChange();
  }
  
  @Override
  public synchronized List<JobTrigger> acquireTriggersInState(long maxTimestamp, int state) {
    
    List<JobTrigger> list = new LinkedList<>();
    
    for (JobTrigger trigger : timeTriggers) {
      if (trigger.getState() == state && trigger.getNextFireTime() != null
          && trigger.getNextFireTime() < maxTimestamp) {
        list.add(trigger);
      }
    }
    return list;
  }
  
  static class JobInfoWrapper {
    
    JobInfo jobInfo;
    final List<JobTrigger> triggers = new LinkedList<>();
    
    JobInfoWrapper(JobInfo jobInfo) {
      this.jobInfo = jobInfo;
    }
  }
  
  static class JobTriggerComparator implements Comparator<JobTrigger> {
    
    @Override
    public int compare(JobTrigger o1, JobTrigger o2) {
      Long nextFireTime1 = o1.getNextFireTime();
      Long nextFireTime2 = o2.getNextFireTime();
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
      return o1.getKey().compareTo(o2.getKey());
    }
    
  }
}
