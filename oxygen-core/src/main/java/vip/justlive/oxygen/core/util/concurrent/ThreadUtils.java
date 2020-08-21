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
package vip.justlive.oxygen.core.util.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.CoreConfigKeys;
import vip.justlive.oxygen.core.util.base.SecurityChecker;
import vip.justlive.oxygen.core.util.concurrent.SecurityThreadPoolExecutor.PoolQueue;
import vip.justlive.oxygen.core.util.timer.WheelTimer;

/**
 * 线程工具类
 *
 * @author wubo
 */
@UtilityClass
public class ThreadUtils {

  private final ThreadLocal<Map<String, Object>> LOCAL = ThreadLocal.withInitial(HashMap::new);

  private ThreadFactory threadFactory;
  private SecurityThreadPoolExecutor globalPool;
  private WheelTimer globalTimer;

  /**
   * 线程存储键值
   *
   * @param key 键
   * @param value 值
   */
  public void putVal(String key, Object value) {
    LOCAL.get().put(key, value);
  }

  /**
   * 获取线程存储的值
   *
   * @param key 键
   * @param <T> 泛型类
   * @return 值
   */
  @SuppressWarnings("unchecked")
  public <T> T getVal(String key) {
    return (T) LOCAL.get().get(key);
  }

  /**
   * 清除线程存储值
   */
  public void clear() {
    LOCAL.remove();
  }

  /**
   * 构造线程池
   *
   * @param corePoolSize 线程数
   * @param maxPoolSize 最大线程数
   * @param keepAliveSeconds 空闲线程等待时间
   * @param queueCapacity 队列大小
   * @param nameFormat 线程名称format
   * @return 线程池
   */
  public SecurityThreadPoolExecutor newThreadPool(int corePoolSize, int maxPoolSize,
      int keepAliveSeconds, int queueCapacity, String nameFormat) {
    return newThreadPool(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, nameFormat,
        true);
  }

  /**
   * 构造线程池
   *
   * @param corePoolSize 线程数
   * @param maxPoolSize 最大线程数
   * @param keepAliveSeconds 空闲线程等待时间
   * @param queueCapacity 队列大小
   * @param nameFormat 线程名称format
   * @param daemon 是否为守护线程
   * @return 线程池
   */
  public SecurityThreadPoolExecutor newThreadPool(int corePoolSize, int maxPoolSize,
      int keepAliveSeconds, int queueCapacity, String nameFormat, boolean daemon) {
    return newThreadPool(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, nameFormat,
        new ThreadPoolExecutor.AbortPolicy(), daemon);
  }

  /**
   * 构造线程池
   *
   * @param corePoolSize 线程数
   * @param maxPoolSize 最大线程数
   * @param keepAliveSeconds 空闲线程等待时间
   * @param queueCapacity 队列大小
   * @param nameFormat 线程名称format
   * @param handler 拒绝策略
   * @return 线程池
   */
  public SecurityThreadPoolExecutor newThreadPool(int corePoolSize, int maxPoolSize,
      int keepAliveSeconds, int queueCapacity, String nameFormat,
      RejectedExecutionHandler handler) {
    return newThreadPool(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, nameFormat,
        handler, true);
  }

  /**
   * 构造线程池
   *
   * @param corePoolSize 线程数
   * @param maxPoolSize 最大线程数
   * @param keepAliveSeconds 空闲线程等待时间
   * @param queueCapacity 队列大小
   * @param nameFormat 线程名称format
   * @param handler 拒绝策略
   * @param daemon 是否为守护线程
   * @return 线程池
   */
  public SecurityThreadPoolExecutor newThreadPool(int corePoolSize, int maxPoolSize,
      int keepAliveSeconds, int queueCapacity, String nameFormat, RejectedExecutionHandler handler,
      boolean daemon) {
    PoolQueue queue = new PoolQueue(queueCapacity);
    SecurityThreadPoolExecutor pool = new SecurityThreadPoolExecutor(corePoolSize, maxPoolSize,
        keepAliveSeconds, TimeUnit.SECONDS, queue,
        new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(daemon).build(), handler);
    queue.setPool(pool);
    return pool;
  }

  /**
   * 构造定时任务池
   *
   * @param corePoolSize 线程数
   * @param nameFormat 线程名称format
   * @return 任务池
   */
  public ScheduledExecutorService newScheduledExecutor(int corePoolSize, String nameFormat) {
    return newScheduledExecutor(corePoolSize, nameFormat, new ThreadPoolExecutor.AbortPolicy());
  }

  /**
   * 构造定时任务池
   *
   * @param corePoolSize 线程数
   * @param nameFormat 线程名称format
   * @param handler 拒绝策略
   * @return 任务池
   */
  public ScheduledExecutorService newScheduledExecutor(int corePoolSize, String nameFormat,
      RejectedExecutionHandler handler) {
    return new ScheduledThreadPoolExecutor(corePoolSize,
        new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(true).build(), handler);
  }

  /**
   * 线程sleep等待
   *
   * @param millis 毫秒值
   */
  public void sleep(long millis) {
    sleep(millis, TimeUnit.MILLISECONDS);
  }

  /**
   * 线程sleep等待
   *
   * @param time 时间
   * @param unit 单位
   */
  public void sleep(long time, TimeUnit unit) {
    try {
      unit.sleep(time);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * 默认全局线程工厂
   *
   * @return ThreadFactory
   */
  public synchronized ThreadFactory defaultThreadFactory() {
    if (threadFactory == null) {
      threadFactory = new ThreadFactoryBuilder().setDaemon(true).setPriority(Thread.NORM_PRIORITY)
          .setNameFormat("def-pool-%d").build();
    }
    return threadFactory;
  }

  /**
   * 全局pool
   *
   * @return pool
   */
  public synchronized SecurityThreadPoolExecutor globalPool() {
    if (globalPool == null) {
      globalPool = newThreadPool(1, CoreConfigKeys.THREAD_POOL_SIZE.castValue(int.class), 120,
          CoreConfigKeys.THREAD_POOL_QUEUE.castValue(int.class), "global-pool-%d");
      globalPool.setSecurityChecker(new SecurityChecker()
          .addChecker(new OwnerThreadChecker(ShutdownHooks.add(globalTimer::shutdown))));
    }
    return globalPool;
  }

  /**
   * 全局时间轮，通过以下参数可进行配置，默认为毫秒级时间轮
   *
   * @return WheelTimer
   */
  public synchronized WheelTimer globalTimer() {
    if (globalTimer == null) {
      globalTimer = new WheelTimer(CoreConfigKeys.WHEEL_TIMER_DURATION.castValue(long.class),
          CoreConfigKeys.WHEEL_TIMER_WHEEL_SIZE.castValue(int.class),
          CoreConfigKeys.WHEEL_TIMER_POOL_SIZE.castValue(int.class));
      globalTimer.getSecurityChecker()
          .addChecker(new OwnerThreadChecker(ShutdownHooks.add(globalTimer::shutdown)));
    }
    return globalTimer;
  }
}
