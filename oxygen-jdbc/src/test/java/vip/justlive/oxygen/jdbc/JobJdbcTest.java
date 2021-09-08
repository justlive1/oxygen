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
package vip.justlive.oxygen.jdbc;

import org.junit.Before;
import org.junit.Test;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.bean.Singleton;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.job.CronJobTrigger;
import vip.justlive.oxygen.core.job.JobInfo;
import vip.justlive.oxygen.core.job.Scheduler;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;
import vip.justlive.oxygen.jdbc.job.JdbcJobStore;

public class JobJdbcTest implements Plugin {

  @Override
  public int order() {
    return Integer.MIN_VALUE + 601;
  }

  @Override
  public void start() {
    Jdbc.update("drop table if exists oxy_job_info");
    Jdbc.update(
        "create table oxy_job_info (id int auto_increment primary key, job_key varchar, description varchar, handler_class varchar, param varchar);");

    Jdbc.update("drop table if exists oxy_job_trigger");
    Jdbc.update(
        "create table oxy_job_trigger (id int auto_increment primary key, job_key varchar, trigger_key varchar, trigger_type int, trigger_value varchar, state int, start_time bigint, end_time bigint, previous_fire_time bigint, next_fire_time bigint, last_completed_time bigint);");

    Jdbc.update("drop table if exists oxy_lock");
    Jdbc.update("create table oxy_lock (id int auto_increment primary key, name varchar);");
    Jdbc.update("insert into oxy_lock (name) values ('trigger_access');");

  }

  @Before
  public void before() {

    ConfigFactory.setProperty("oxygen.job.jobStoreClass", JdbcJobStore.class.getName());

    Bootstrap.addCustomPlugin(new JobJdbcTest());
    Bootstrap.start();


  }

  @Test
  public void test() {

    Scheduler scheduler = Singleton.get(Scheduler.class);

    scheduler.scheduleJob(new JobInfo().setKey("job-1").setDescription("test 1")
        .setHandlerClass(HelloJob.class.getName()), new CronJobTrigger("job-1", "0/5 * * * * ?"));

    ThreadUtils.sleep(100000);

  }
}
