/*
 * Copyright (C) 2019 the original author or authors.
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
package vip.justlive.oxygen.job;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * job类
 *
 * @author wubo
 */
@Slf4j
public class Job implements Runnable {

  private final Object target;
  private final Method method;
  private final AtomicLong runCount = new AtomicLong();
  private final AtomicLong nextPlannedExecution = new AtomicLong();
  private TYPE type;
  private Long fixedDelay;
  private Long fixedRate;
  private Long initialDelay;
  private String cron;
  private Boolean async;
  private CronExpression cronExpression;
  private Instant startAt;

  public Job(Object target, Method method) {
    this.target = target;
    this.method = method;
  }

  void scheduleCron() {
    if (cron != null && cronExpression != null) {
      Date now = new Date();
      Date nextDate = cronExpression.next(now);
      if (nextDate == null) {
        log.warn(
            "The cron expression for job %s doesn't have any match in the future, will never be executed [{}]",
            this);
        return;
      }
      if (nextPlannedExecution.get() == nextDate.getTime()) {
        //避免同时运行两次作业（当我们在计划时间前几分钟运行作业时发生）
        Date nextInvalid = cronExpression.next(nextDate);
        nextDate = cronExpression.next(nextInvalid);
      }
      nextPlannedExecution.set(nextDate.getTime());
      JobPlugin.executorService
          .schedule(this, nextDate.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
    }
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
    this.cronExpression = new CronExpression(cron);
    return this;
  }

  public Job configOnApplicationStart(boolean async) {
    this.type = TYPE.ON_APPLICATION_START;
    this.async = async;
    return this;
  }

  @Override
  public void run() {
    try {
      before();
      method.invoke(target);
      after();
    } catch (Exception e) {
      onException(e);
    } finally {
      onFinally();
    }
  }

  private void before() {
    startAt = Instant.now();
  }

  private void after() {
    if (log.isDebugEnabled()) {
      log.debug("job [{}] execute time [{}]", this,
          Duration.between(startAt, Instant.now()).toMillis());
    }
  }

  private void onException(Exception e) {
    log.error("execute job [{}] error", this, e);
  }

  private void onFinally() {
    runCount.getAndIncrement();
    scheduleCron();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Job: type=").append(type);
    sb.append(", class=").append(target.getClass()).append(", method=").append(method.getName());
    if (type == TYPE.CRON) {
      sb.append(",cron=").append(cron);
    } else if (type == TYPE.ON_APPLICATION_START) {
      sb.append(",async=").append(async);
    } else {
      if (type == TYPE.FIXED_DELAY) {
        sb.append(",fixedDelay=").append(fixedDelay);
      } else if (type == TYPE.FIXED_RATE) {
        sb.append(",fixedRate=").append(fixedRate);
      }
      sb.append(",initialDelay=").append(initialDelay);
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
