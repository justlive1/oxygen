/*
 * Copyright (C) 2021 the original author or authors.
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

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.bean.Singleton;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * scheduler工厂类
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class SchedulerFactory {

  public Scheduler getScheduler(JobConf conf, SchedulerPlugin... plugins) {
    JobStore jobStore = null;
    if (Strings.hasText(conf.getJobStoreClass())) {
      try {
        Class<?> jobStoreClass = ClassUtils.forName(conf.getJobStoreClass());
        jobStore = (JobStore) ClassUtils.newInstance(jobStoreClass);
      } catch (Exception e) {
        log.warn("load jobStoreClass failed.", e);
      }
    }
    if (jobStore == null) {
      jobStore = Singleton.get(JobStore.class);
    }
    if (jobStore == null) {
      jobStore = new MapJobStore();
    }

    JobThreadPool pool = null;
    if (Strings.hasText(conf.getJobThreadPoolClass())) {
      try {
        Class<?> jobThreadPoolClass = ClassUtils.forName(conf.getJobStoreClass());
        pool = (JobThreadPool) ClassUtils.newInstance(jobThreadPoolClass);
      } catch (Exception e) {
        log.warn("load JobThreadPool failed.", e);
      }
    }
    if (pool == null) {
      pool = Singleton.get(JobThreadPool.class);
    }
    if (pool == null) {
      pool = new SimpleJobThreadPool(conf);
    }
    log.info("create scheduler with {} and {}", jobStore, pool);

    JobResource resource = new JobResource(conf, jobStore, pool);

    SchedulerImpl scheduler = new SchedulerImpl(resource);

    // plugins
    addSchedulerPlugin(new MisfireSchedulerPlugin(), resource, scheduler);
    addSchedulerPlugin(new LostSchedulerPlugin(), resource, scheduler);

    if (plugins == null || plugins.length == 0) {
      return scheduler;
    }

    for (SchedulerPlugin plugin : plugins) {
      addSchedulerPlugin(plugin, resource, scheduler);
    }

    return scheduler;
  }

  private void addSchedulerPlugin(SchedulerPlugin plugin, JobResource resource,
      Scheduler scheduler) {
    plugin.initialize(resource, scheduler);
    resource.getSchedulerPlugins().add(plugin);
  }

}
