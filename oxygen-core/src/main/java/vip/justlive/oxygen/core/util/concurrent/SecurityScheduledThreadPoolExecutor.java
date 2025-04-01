/*
 * Copyright (C) 2025 the original author or authors.
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

import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;
import vip.justlive.oxygen.core.util.base.SecurityChecker;

/**
 * 增加安全校验的线程池
 *
 * @author wubo
 */
@Getter
public class SecurityScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

  private static final AtomicInteger COUNT = new AtomicInteger();

  private final String name;

  @Setter
  private SecurityChecker securityChecker;

  public SecurityScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory,
      RejectedExecutionHandler handler) {
    this(SecurityThreadPoolExecutor.UNNAMED_PREFIX + COUNT.getAndIncrement(), corePoolSize,
        threadFactory, handler);
  }

  public SecurityScheduledThreadPoolExecutor(String name, int corePoolSize,
      ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    super(corePoolSize, threadFactory, handler);
    this.name = name;
    ThreadPoolMonitor.add(this);
  }

  @Override
  public List<Runnable> shutdownNow() {
    if (securityChecker != null) {
      securityChecker.checkPermission();
    }
    ThreadPoolMonitor.remove(this);
    return super.shutdownNow();
  }

  @Override
  public void shutdown() {
    if (securityChecker != null) {
      securityChecker.checkPermission();
    }
    ThreadPoolMonitor.remove(this);
    super.shutdown();
  }

  @Override
  public String toString() {
    return super.toString() + "-[pool name = " + name + ", maximum pool size = "
        + getMaximumPoolSize() + "]";
  }
}
