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

import java.util.concurrent.ThreadPoolExecutor;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * 简单job线程池实现
 *
 * @author wubo
 */
public class SimpleJobThreadPool implements JobThreadPool {

  private final ThreadPoolExecutor pool;

  public SimpleJobThreadPool(int corePoolSize, String threadNameFormat) {
    pool = ThreadUtils.newThreadPool(1, corePoolSize, 1, 1000, threadNameFormat);
  }

  @Override
  public void execute(Runnable runnable) {
    pool.execute(runnable);
  }
}
