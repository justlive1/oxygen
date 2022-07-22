/*
 * Copyright (C) 2022 the original author or authors.
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 每个容器只能同一时间只能执行一次的任务
 *
 * @author wubo
 */
@Slf4j
public abstract class OncePerInstanceJob implements Job {
  
  private static final Map<Class<?>, Job> RUNNING_JOBS = new ConcurrentHashMap<>();
  
  /**
   * 实际执行逻辑
   *
   * @param ctx job上下文
   */
  public abstract void doExecute(JobContext ctx);
  
  @Override
  public void execute(JobContext ctx) {
    Class<?> clazz = getClass();
    Job existed = RUNNING_JOBS.putIfAbsent(clazz, this);
    if (existed != null) {
      conflict(ctx, existed);
      return;
    }
    
    try {
      doExecute(ctx);
    } finally {
      RUNNING_JOBS.remove(clazz);
    }
  }
  
  public void conflict(JobContext ctx, Job existed) {
    log.info("当前环境中已存在正在运行的任务[{}]，此次任务[{}]跳过", existed, ctx.getParam());
  }
}
