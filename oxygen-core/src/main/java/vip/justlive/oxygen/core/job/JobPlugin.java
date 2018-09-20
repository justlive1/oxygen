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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.ioc.BeanStore;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;
import vip.justlive.oxygen.core.util.ThreadUtils;

/**
 * job插件
 *
 * @author wubo
 */
public class JobPlugin implements Plugin {

  private static final Map<Class<?>, Object> JOB_CACHE = new ConcurrentHashMap<>(8, 1F);
  private static final List<Job> SCHEDULED_JOBS = new ArrayList<>();
  static ScheduledExecutorService executorService;

  private static void init() {
    int poolSize = Constants.DEFAULT_JOB_CORE_POOL_SIZE;
    String jobCorePoolSize = ConfigFactory.getProperty(Constants.JOB_CORE_POOL_SIZE_KEY);
    if (jobCorePoolSize != null) {
      poolSize = Integer.parseInt(jobCorePoolSize);
    }
    String jobNameFormat = ConfigFactory.getProperty(Constants.JOB_THREAD_NAME_FORMAT_KEY,
        Constants.DEFAULT_JOB_THREAD_NAME_FORMAT);
    executorService = ThreadUtils.newScheduledExecutor(poolSize, jobNameFormat);
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
    JOB_CACHE.clear();
  }

  private void parseJobs() {
    Set<Method> methods = ClassScannerPlugin.getMethodsAnnotatedWith(Scheduled.class);
    for (Method method : methods) {
      Job job = createJob(method);
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

  private Job createJob(Method method) {
    Object target = getDeclaringBean(method);
    return new Job(target, method);
  }

  private Object getDeclaringBean(Method method) {
    Class<?> beanClass = method.getDeclaringClass();
    Object bean = BeanStore.getBean(beanClass);
    if (bean != null) {
      return bean;
    }
    bean = JOB_CACHE.get(beanClass);
    if (bean != null) {
      return bean;
    }
    try {
      bean = beanClass.newInstance();
      JOB_CACHE.putIfAbsent(beanClass, bean);
      return JOB_CACHE.get(beanClass);
    } catch (InstantiationException | IllegalAccessException e) {
      throw Exceptions.wrap(e);
    }
  }

}
