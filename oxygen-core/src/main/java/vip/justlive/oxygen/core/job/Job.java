/*
 * Copyright (C) 2018 justlive1
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
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * job类
 *
 * @author wubo
 */
@Slf4j
public class Job implements Runnable {

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
    ON_APPLICATION_START;
  }

  private final Object target;
  private final Method method;
  private TYPE type;
  private Long fixedDelay;
  private Long fixedRate;
  private Long initialDelay;
  private String cron;
  private Boolean onApplicationStart;
  private Boolean async;
  private CronExpression cronExpression;
  private final AtomicLong runCount = new AtomicLong();
  private final AtomicLong nextPlannedExecution = new AtomicLong();
  private Instant startAt;


  public Job(Object target, Method method) {
    this.target = target;
    this.method = method;
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
    try {
      this.cronExpression = new CronExpression(cron);
    } catch (ParseException e) {
      throw Exceptions.wrap(e);
    }
    return this;
  }

  public Job configOnApplicationStart(boolean onApplicationStart, boolean async) {
    this.type = TYPE.ON_APPLICATION_START;
    this.onApplicationStart = onApplicationStart;
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
      log.debug("job [{}] [{}] execute time [{}]", method.getDeclaringClass(), method.getName(),
          Duration.between(startAt, Instant.now()).toMillis());
    }
  }

  private void onException(Exception e) {
    log.error("execute job error", e);
  }

  private void onFinally() {
    runCount.getAndIncrement();
    scheduleCron();
  }

  protected void scheduleCron() {
    if (cron != null && cronExpression != null) {
      Date now = new Date();
      Date nextDate = cronExpression.getNextValidTimeAfter(now);
      if (nextDate == null) {
        log.warn(
            "The cron expression for job %s doesn't have any match in the future, will never be executed [{}] [{}]",
            method.getDeclaringClass(), method.getName());
        return;
      }
      if (nextPlannedExecution.get() == nextDate.getTime()) {
        //避免同时运行两次作业（当我们在计划时间前几分钟运行作业时发生）
        Date nextInvalid = cronExpression.getNextInvalidTimeAfter(nextDate);
        nextDate = cronExpression.getNextValidTimeAfter(nextInvalid);
      }
      nextPlannedExecution.set(nextDate.getTime());
      JobPlugin.executorService
          .schedule(this, nextDate.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
    }
  }
}
