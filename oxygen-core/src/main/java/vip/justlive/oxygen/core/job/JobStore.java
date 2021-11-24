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

/**
 * job存储
 *
 * @author wubo
 */
public interface JobStore {
  
  /**
   * 初始化信号器
   *
   * @param signaler 信号器
   */
  void initialize(Signaler signaler);
  
  /**
   * 保存job
   *
   * @param jobInfo         job信息
   * @param replaceExisting 是否替换已存在
   */
  void storeJob(JobInfo jobInfo, boolean replaceExisting);
  
  /**
   * 根据jobKey获取任务信息
   *
   * @param jobKey 任务key
   * @return 任务信息
   */
  JobInfo getJobInfo(String jobKey);
  
  /**
   * 删除任务
   *
   * @param jobKey 任务key
   */
  void removeJob(String jobKey);
  
  /**
   * 保存任务触发器
   *
   * @param trigger         触发器
   * @param replaceExisting 是否替换已存在
   */
  default void storeTrigger(JobTrigger trigger, boolean replaceExisting) {
    storeTrigger(trigger, JobConstants.STATE_WAITING, replaceExisting);
  }
  
  /**
   * 保存任务触发器
   *
   * @param trigger         触发器
   * @param state           状态
   * @param replaceExisting 是否替换已存在
   */
  void storeTrigger(JobTrigger trigger, int state, boolean replaceExisting);
  
  
  /**
   * 获取任务下关联的触发器
   *
   * @param jobKey 任务key
   * @return list
   */
  List<JobTrigger> getJobTrigger(String jobKey);
  
  /**
   * 删除触发器
   *
   * @param triggerKey 触发器key
   */
  void removeTrigger(String triggerKey);
  
  /**
   * 暂停任务
   *
   * @param jobKey 任务key
   */
  void pauseJob(String jobKey);
  
  /**
   * 暂停触发器
   *
   * @param triggerKey 触发器key
   */
  void pauseTrigger(String triggerKey);
  
  /**
   * 恢复任务
   *
   * @param jobKey 任务key
   */
  void resumeJob(String jobKey);
  
  /**
   * 恢复触发器
   *
   * @param triggerKey 触发器key
   */
  void resumeTrigger(String triggerKey);
  
  /**
   * 获取最近可执行的触发器
   *
   * @param maxTimestamp 最大时间戳
   * @param maxSize      获取最大数量
   * @return list
   */
  List<JobTrigger> acquireNextTriggers(long maxTimestamp, int maxSize);
  
  /**
   * 释放获取的触发器
   *
   * @param trigger 触发器
   */
  void releaseTrigger(JobTrigger trigger);
  
  /**
   * 触发器准备执行
   *
   * @param trigger 触发器
   * @return result
   */
  TriggerFiredResult triggerFired(JobTrigger trigger);
  
  /**
   * 触发器执行完成
   *
   * @param trigger 触发器
   * @param state   状态
   */
  void triggerCompleted(JobTrigger trigger, int state);
  
  /**
   * 获取触发器
   *
   * @param maxTimestamp 最大时间戳
   * @param state        状态
   * @return 触发器
   */
  List<JobTrigger> acquireTriggersInState(long maxTimestamp, int state);
  
}
