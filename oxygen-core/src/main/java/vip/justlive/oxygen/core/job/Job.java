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
package vip.justlive.oxygen.core.job;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.aop.invoke.Invoker;
import vip.justlive.oxygen.core.util.base.ClassUtils;

/**
 * job类
 *
 * @author wubo
 */
@Slf4j
public class Job implements Runnable {

  private final Object target;
  private final Method method;
  private final Invoker invoker;
  private final AtomicLong runCount = new AtomicLong();
  private TYPE type;
  private Long fixedDelay;
  private Long fixedRate;
  private Long initialDelay;
  private String cron;
  private Boolean async;
  private Instant startAt;

  public Job(Object target, Method method) {
    this.target = target;
    this.method = method;
    this.invoker = ClassUtils.generateInvoker(target, method);
  }

  public Job configFixedDelay(long fixedDelay, long initialDelay) {
    this.type = TYPE.FIXED_DELAY;
    this.fixedDelay = fixedDelay;
    this.initialDelay = initialDelay;
    return this;
  }

  public Job configFixedRate(long fixedRate, long initialDelay) {
    this.type = TYPE.FIXED_RATE;
    this.fixedRate = fixedRate;
    this.initialDelay = initialDelay;
    return this;
  }

  public Job configCron(String cron) {
    this.type = TYPE.CRON;
    this.cron = cron;
    return this;
  }

  public Job configOnApplicationStart(boolean async) {
    Job job = this;
    if (this.type != null) {
      job = new Job(target, method);
    }
    job.type = TYPE.ON_APPLICATION_START;
    job.async = async;
    return job;
  }

  @Override
  public void run() {
    try {
      startAt = Instant.now();
      invoker.invoke();
    } catch (Exception e) {
      onException(e);
    } finally {
      onFinally();
    }
  }

  private void onException(Exception e) {
    log.error("job [{}] execute error", this, e);
  }

  private void onFinally() {
    long count = runCount.incrementAndGet();
    if (log.isDebugEnabled()) {
      log.debug("job [{}] elapsed time [{}], current rounds [{}]", this,
          Duration.between(startAt, Instant.now()).toMillis(), count);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(target.getClass().getName()).append("#")
        .append(method.getName()).append("|").append(type);
    if (type == TYPE.CRON) {
      sb.append("|").append(cron);
    } else if (type == TYPE.ON_APPLICATION_START) {
      sb.append("|").append(async);
    } else {
      if (type == TYPE.FIXED_DELAY) {
        sb.append("|").append(fixedDelay);
      } else if (type == TYPE.FIXED_RATE) {
        sb.append("|").append(fixedRate);
      }
      sb.append(",").append(initialDelay);
    }
    return sb.toString();
  }

  /**
   * job类型
   */
  public enum TYPE {
    /**
     * fixed delay job
     */
    FIXED_DELAY,
    /**
     * fixed rate job
     */
    FIXED_RATE,
    /**
     * cron job
     */
    CRON,
    /**
     * when application start
     */
    ON_APPLICATION_START
  }
}
