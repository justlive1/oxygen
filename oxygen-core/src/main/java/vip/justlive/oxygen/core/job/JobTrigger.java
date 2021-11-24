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
 * job触发器
 *
 * @author wubo
 */
public interface JobTrigger {
  
  /**
   * 触发器key
   *
   * @return key
   */
  String getKey();
  
  /**
   * 任务key
   *
   * @return jobKey
   */
  String getJobKey();
  
  /**
   * 上一次执行时间
   *
   * @return 上一次执行时间
   */
  Long getPreviousFireTime();
  
  /**
   * 下一次执行时间
   *
   * @return 下一次执行时间
   */
  Long getNextFireTime();
  
  /**
   * 获取指定时间后最近一次执行时间
   *
   * @param timestamp 时间戳
   * @return 时间戳
   */
  Long getFireTimeAfter(long timestamp);
  
  /**
   * 设置最近一次完成时间
   *
   * @param timestamp 时间戳
   */
  void setLastCompletedTime(Long timestamp);
  
  /**
   * 执行器执行时触发，用于计算下一次执行时间
   *
   * @param timestamp 时间戳
   * @return 时间戳
   */
  Long triggerFired(long timestamp);
  
  
  /**
   * 计算下一次执行时间，启动和恢复任务时调用
   *
   * @return 时间戳
   */
  default Long computeNextFireTime() {
    return triggerFired(System.currentTimeMillis());
  }
  
  /**
   * 获取当前状态
   *
   * @return state
   */
  Integer getState();
  
  /**
   * 设置状态
   *
   * @param state 状态
   */
  void setState(Integer state);
}
