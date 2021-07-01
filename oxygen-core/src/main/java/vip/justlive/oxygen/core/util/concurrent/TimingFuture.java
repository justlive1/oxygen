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
package vip.justlive.oxygen.core.util.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 特定时机进行处理
 *
 * @author wubo
 */
public interface TimingFuture<T> extends Future<T> {

  /**
   * 多久后仍在执行中则触发任务
   *
   * @param runnable 任务
   * @param time     时间
   * @param unit     时间单位
   * @return true表示添加成功
   */
  boolean afterRunning(Runnable runnable, long time, TimeUnit unit);
}
