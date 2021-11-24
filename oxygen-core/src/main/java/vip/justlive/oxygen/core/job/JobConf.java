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

import lombok.Data;
import vip.justlive.oxygen.core.config.ValueConfig;

/**
 * job配置
 *
 * @author wubo
 */
@Data
@ValueConfig("oxygen.job")
public class JobConf {

  private String jobStoreClass;
  private String jobThreadPoolClass;

  private int threadCorePoolSize = 10;
  private String threadNameFormat = "job-%d";
  private int threadQueueCapacity = 1000;

  private long slowTimeWindow = 60000;
  private long slowThresholdTime = 500;
  private int slowHitLimit = 10;

  private int fetchMaxSize = 100;
  private long idleWaitTime = 30000;
  private int idleWaitRandom = 7 * 1000;
  private long misfireThreshold = 60000;
  private long lostThreshold = 600000;
}
