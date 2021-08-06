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

/**
 * 调度执行器
 *
 * @author wubo
 */
public interface Scheduler {

  /**
   * 启动
   */
  void start();

  /**
   * 停止
   */
  void shutdown();

  /**
   * 是否停止
   *
   * @return true为停止
   */
  boolean isShutdown();

  /**
   * 添加任务
   *
   * @param jobInfo         任务信息
   * @param replaceExisting 已存在是否替换
   */
  void addJob(JobInfo jobInfo, boolean replaceExisting);

  /**
   * 添加任务并进行调度
   *
   * @param jobInfo 任务信息
   * @param trigger 触发器
   */
  void scheduleJob(JobInfo jobInfo, JobTrigger trigger);

  /**
   * 调度任务
   *
   * @param trigger 触发器
   */
  void scheduleJob(JobTrigger trigger);

  /**
   * 删除任务
   *
   * @param jobKey 任务key
   */
  void removeJob(String jobKey);

  /**
   * 执行一次任务
   *
   * @param jobKey 任务key
   */
  void triggerJob(String jobKey);

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

}
