/*
 * Copyright (C) 2020 the original author or authors.
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
package vip.justlive.oxygen.core.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 关闭钩子
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class ShutdownHooks {

  private final AtomicBoolean STATE = new AtomicBoolean(false);
  private final List<Runnable> TASKS = new ArrayList<>();
  private final Thread HOOK = ThreadUtils.defaultThreadFactory().newThread(ShutdownHooks::runTasks);

  /**
   * 添加钩子
   *
   * @param task 任务
   * @return 执行线程
   */
  public synchronized Thread add(final Runnable task) {
    Objects.requireNonNull(task);
    if (STATE.compareAndSet(false, true)) {
      log.info("Registering shutdown-hook: {}", HOOK);
      Runtime.getRuntime().addShutdownHook(HOOK);
    }
    log.info("Adding shutdown-hook task: {}", task);
    TASKS.add(task);
    return HOOK;
  }

  /**
   * 删除钩子
   *
   * @param task 任务
   */
  public synchronized void remove(final Runnable task) {
    Objects.requireNonNull(task);
    log.info("Removing shutdown-hook tasks: {}", task);
    TASKS.remove(task);
  }

  private synchronized void runTasks() {
    log.info("Running all shutdown-hook tasks");
    for (Runnable task : TASKS) {
      log.info("Running task: {}", task);
      try {
        task.run();
      } catch (Throwable e) {
        log.warn("Task failed", e);
      }
    }
    TASKS.clear();
  }

}
