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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import lombok.Data;
import vip.justlive.oxygen.core.util.concurrent.RepeatRunnable;

/**
 * job资源
 *
 * @author wubo
 */
@Data
public class JobResource {

  private final JobConf conf;
  private final JobStore jobStore;
  private final JobThreadPool pool;

  private RepeatRunnable schedulerThread;
  private Signaler signaler;

  Map<String, List<WaitingTaskFuture>> futures = new ConcurrentHashMap<>(4);
  List<SchedulerPlugin> schedulerPlugins = new ArrayList<>();

  static class WaitingTaskFuture {

    long nextFireTime;
    Future<?> future;
  }

}
