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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.CoreConfigKeys;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.bean.Singleton;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.concurrent.ThreadFactoryBuilder;
import vip.justlive.oxygen.core.util.timer.WheelTimer;

/**
 * job插件
 *
 * @author wubo
 */
@Slf4j
public class JobPlugin implements Plugin {

  private static final List<Job> SCHEDULED_JOBS = new ArrayList<>();

  private WheelTimer wheelTimer;

  /**
   * 获取当前job数量
   *
   * @return size of scheduled jobs
   */
  public static int currentJobSize() {
    return SCHEDULED_JOBS.size();
  }

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
    wheelTimer.shutdown();
    SCHEDULED_JOBS.clear();
  }

  private void init() {
    wheelTimer = new WheelTimer(1, 60, CoreConfigKeys.JOB_CORE_POOL_SIZE.castValue(int.class),
        new ThreadFactoryBuilder().setDaemon(true)
            .setNameFormat(CoreConfigKeys.JOB_THREAD_NAME_FORMAT.getValue()).build());
  }

  private void parseJobs() {
    Singleton.getAll().forEach(
        bean -> ClassUtils.getMethodsAnnotatedWith(bean.getClass(), Scheduled.class)
            .forEach(method -> handleMethod(method, bean)));
  }

  private void handleMethod(Method method, Object bean) {
    Job job;
    try {
      job = new Job(bean, bean.getClass().getMethod(method.getName(), method.getParameterTypes()));
    } catch (NoSuchMethodException e) {
      throw Exceptions.wrap(e);
    }
    Scheduled scheduled = method.getAnnotation(Scheduled.class);
    check(scheduled);
    if (scheduled.fixedDelay().length() > 0) {
      addFixedDelayJob(job, scheduled.fixedDelay(), scheduled.initialDelay());
    }
    if (scheduled.fixedRate().length() > 0) {
      addFixedRateJob(job, scheduled.fixedRate(), scheduled.initialDelay());
    }
    if (scheduled.cron().length() > 0) {
      addCronJob(job, scheduled.cron());
    }
    if (scheduled.onApplicationStart()) {
      addOnApplicationStartJob(job, scheduled.async());
    }
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
    boolean invalid = count > 1 || (count == 0 && !scheduled.onApplicationStart());
    if (invalid) {
      throw new IllegalArgumentException(
          "Specify 'fixedDelay', 'fixedRate', 'onApplicationStart' or 'cron'");
    }
  }

  private void addFixedDelayJob(Job job, String fixedDelay, String initialDelay) {
    long fixedDelayVal = Long.parseLong(ConfigFactory.getPlaceholderProperty(fixedDelay));
    long initialDelayVal = 0;
    if (initialDelay.length() > 0) {
      initialDelayVal = Long.parseLong(ConfigFactory.getPlaceholderProperty(initialDelay));
    }
    wheelTimer.scheduleWithFixedDelay(job.configFixedDelay(fixedDelayVal, initialDelayVal),
        initialDelayVal, fixedDelayVal, TimeUnit.MILLISECONDS);
    addJob(job);
  }

  private void addFixedRateJob(Job job, String fixedRate, String initialDelay) {
    long fixedRateVal = Long.parseLong(ConfigFactory.getPlaceholderProperty(fixedRate));
    long initialDelayVal = 0;
    if (initialDelay.length() > 0) {
      initialDelayVal = Long.parseLong(ConfigFactory.getPlaceholderProperty(initialDelay));
    }
    wheelTimer
        .scheduleAtFixedRate(job.configFixedRate(fixedRateVal, initialDelayVal), initialDelayVal,
            fixedRateVal, TimeUnit.MILLISECONDS);
    addJob(job);
  }

  private void addCronJob(Job job, String cron) {
    cron = ConfigFactory.getPlaceholderProperty(cron);
    wheelTimer.scheduleOnCron(job.configCron(cron), cron);
    addJob(job);
  }

  private void addOnApplicationStartJob(Job job, boolean async) {
    Job target = job.configOnApplicationStart(async);
    if (async) {
      wheelTimer.schedule(target, 0, TimeUnit.MILLISECONDS);
    } else {
      target.run();
    }
    addJob(target);
  }

  private void addJob(Job job) {
    if (log.isDebugEnabled()) {
      log.debug("add a job [{}]", job);
    }
    SCHEDULED_JOBS.add(job);
  }

}
