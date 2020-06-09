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

import java.util.concurrent.TimeUnit;

/**
 * @author wubo
 */
public class Conf {

  @Scheduled(fixedDelay = "500")
  public void run1() throws InterruptedException {
    System.out.println(Thread.currentThread() + "|fixedDelay|" + System.currentTimeMillis());
    TimeUnit.MILLISECONDS.sleep(300);
  }

  @Scheduled(fixedRate = "${job.run2:500}")
  public void run2() {
    System.out.println(Thread.currentThread() + "|fixedRate|" + System.currentTimeMillis());
  }

  @Scheduled(onApplicationStart = true, cron = "0/3 * * * * ?", async = true)
  public void run4() {
    System.out.println(Thread.currentThread() + "|cron|" + System.currentTimeMillis());
  }
}
