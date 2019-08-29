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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.ThreadUtils;
import vip.justlive.oxygen.ioc.IocPlugin;

/**
 * job插件
 *
 * @author wubo
 */
@Slf4j
public class JobPlugin implements Plugin {

  private static final List<Job> SCHEDULED_JOBS = new ArrayList<>();
  static ScheduledExecutorService executorService;

  private static void init() {
    CoreConf config = ConfigFactory.load(CoreConf.class);
    executorService = ThreadUtils
        .newScheduledExecutor(config.getJobPoolSize(), config.getJobThreadFormat());
  }

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
    return Integer.MIN_VALUE + 30;
  }

  @Override
  public void start() {
    init();
    parseJobs();
  }

  @Override
  public void stop() {
    executorService.shutdownNow();
    SCHEDULED_JOBS.clear();
  }

  private void parseJobs() {
    IocPlugin.beanStore().getBeans().forEach(
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
    if (log.isDebugEnabled()) {
      log.debug("add a job [{}]", job);
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
    long fixedDelayVal = Long.parseLong(ConfigFactory.getProperty(fixedDelay, fixedDelay));
    long initialDelayVal = 0;
    if (initialDelay.length() > 0) {
      initialDelayVal = Long.parseLong(ConfigFactory.getProperty(initialDelay, initialDelay));
    }
    job.configFixedDelay(fixedDelayVal, initialDelayVal);
    executorService
        .scheduleWithFixedDelay(job, initialDelayVal, fixedDelayVal, TimeUnit.MILLISECONDS);
    SCHEDULED_JOBS.add(job);
  }

  private void addFixedRateJob(Job job, String fixedRate, String initialDelay) {
    long fixedRateVal = Long.parseLong(ConfigFactory.getProperty(fixedRate, fixedRate));
    long initialDelayVal = 0;
    if (initialDelay.length() > 0) {
      initialDelayVal = Long.parseLong(ConfigFactory.getProperty(initialDelay, initialDelay));
    }
    job.configFixedRate(fixedRateVal, initialDelayVal);
    executorService.scheduleAtFixedRate(job, initialDelayVal, fixedRateVal, TimeUnit.MILLISECONDS);
    SCHEDULED_JOBS.add(job);
  }

  private void addCronJob(Job job, String cron) {
    job.configCron(ConfigFactory.getProperty(cron, cron));
    job.scheduleCron();
    SCHEDULED_JOBS.add(job);
  }

  private void addOnApplicationStartJob(Job job, boolean async) {
    job.configOnApplicationStart(async);
    if (async) {
      executorService.submit(job);
    } else {
      job.run();
    }
    SCHEDULED_JOBS.add(job);
  }

}
