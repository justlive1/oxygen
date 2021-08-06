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
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.aop.invoke.Invoker;
import vip.justlive.oxygen.core.bean.Singleton;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * job插件
 *
 * @author wubo
 */
@Slf4j
public class JobPlugin implements Plugin {

  private Scheduler scheduler;

  @Override
  public int order() {
    return Integer.MIN_VALUE + 400;
  }

  @Override
  public void start() {
    init();
    parseJobs();
  }

  @Override
  public void stop() {
    scheduler.shutdown();
  }

  private void init() {
    JobConf conf = ConfigFactory.load(JobConf.class);
    scheduler = SchedulerFactory.getScheduler(conf);
    scheduler.start();
  }

  private void parseJobs() {
    Singleton.getAll().forEach(
        bean -> ClassUtils.getMethodsAnnotatedWith(bean.getClass(), Scheduled.class)
            .forEach(method -> handleMethod(method, bean)));
  }

  private void handleMethod(Method method, Object bean) {
    JobInfo jobInfo = new JobInfo().setKey(
        ClassUtils.getActualClass(bean.getClass()).getName() + Strings.DOT + method.getName())
        .setHandlerClass(AnnotationJob.class.getName());

    Scheduled scheduled = method.getAnnotation(Scheduled.class);
    check(scheduled);

    JobTrigger trigger = null;
    if (scheduled.fixedDelay().length() > 0) {
      long fixedDelayVal = Long
          .parseLong(ConfigFactory.getPlaceholderProperty(scheduled.fixedDelay()));
      long initialDelayVal = 0;
      if (scheduled.initialDelay().length() > 0) {
        initialDelayVal = Long
            .parseLong(ConfigFactory.getPlaceholderProperty(scheduled.initialDelay()));
      }
      trigger = new DelayOrRateJobTrigger(jobInfo.getKey(), initialDelayVal, fixedDelayVal, true);
    }
    if (scheduled.fixedRate().length() > 0) {
      long fixedRateVal = Long
          .parseLong(ConfigFactory.getPlaceholderProperty(scheduled.fixedRate()));
      long initialDelayVal = 0;
      if (scheduled.initialDelay().length() > 0) {
        initialDelayVal = Long
            .parseLong(ConfigFactory.getPlaceholderProperty(scheduled.initialDelay()));
      }
      trigger = new DelayOrRateJobTrigger(jobInfo.getKey(), initialDelayVal, fixedRateVal, false);
    }
    if (scheduled.cron().length() > 0) {
      String cron = ConfigFactory.getPlaceholderProperty(scheduled.cron());
      trigger = new CronJobTrigger(jobInfo.getKey(), cron);
    }

    log.info("job : {} {}", jobInfo, trigger);
    if (trigger == null) {
      return;
    }

    Invoker invoker;
    try {
      invoker = ClassUtils.generateInvoker(bean,
          bean.getClass().getMethod(method.getName(), method.getParameterTypes()));
    } catch (NoSuchMethodException e) {
      throw Exceptions.wrap(e);
    }
    AnnotationJob.put(jobInfo.getKey(), invoker);
    scheduler.scheduleJob(jobInfo, trigger);
  }

  private void check(Scheduled scheduled) {
    int count = 0;
    if (scheduled.fixedDelay().length() > 0) {
      count++;
    }
    if (scheduled.fixedRate().length() > 0) {
      count++;
    }
    if (scheduled.cron().length() > 0) {
      count++;
    }
    if (count > 1 || count == 0) {
      throw new IllegalArgumentException(
          "Specify 'fixedDelay', 'fixedRate', 'onApplicationStart' or 'cron'");
    }
  }

}
