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

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

@Slf4j
class SchedulerFactoryTest {

  @Test
  void test() {

    ThreadUtils.globalTimer().start();

    JobConf conf = new JobConf();
    Scheduler scheduler = SchedulerFactory.getScheduler(conf);

    scheduler.start();

    JobInfo jobInfo = new JobInfo().setKey("j1").setHandlerClass(HelloJob.class.getName());

    scheduler.scheduleJob(jobInfo, new CronJobTrigger(jobInfo.getKey(), "0/5 * * * * ?"));

    JobResource resource = (JobResource) ClassUtils.getValue(scheduler, "resource");

    ThreadUtils.sleep(8000);

    log.info("pause job");
    scheduler.pauseJob(jobInfo.getKey());
    assertEquals(0, resource.futures.size());

    jobInfo = new JobInfo().setKey("j2").setHandlerClass(HelloJob1.class.getName());
    scheduler.scheduleJob(jobInfo, new DelayOrRateJobTrigger(jobInfo.getKey(), 3000, true));

    ThreadUtils.sleep(8000);

    scheduler.shutdown();
  }

  @Test
  void test1() {

    ThreadUtils.globalTimer().start();

    JobConf conf = new JobConf();
    Scheduler scheduler = SchedulerFactory.getScheduler(conf);

    scheduler.start();

    JobInfo jobInfo = new JobInfo().setKey("j1").setHandlerClass(HelloJob.class.getName());

    scheduler.scheduleJob(jobInfo, new DelayOrRateJobTrigger(jobInfo.getKey(), 500, 300, false));

    ThreadUtils.sleep(18000);

    scheduler.shutdown();
  }
}