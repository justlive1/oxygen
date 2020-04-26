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
package vip.justlive.oxygen.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.util.timer.WheelTimer;

/**
 * 线程工具类
 *
 * @author wubo
 */
@UtilityClass
public class ThreadUtils {

  private final ThreadLocal<Map<String, Object>> LOCAL = ThreadLocal
      .withInitial(ConcurrentHashMap::new);

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
    return new SecurityThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds,
        TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity),
        new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(daemon).build(), handler);
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
   * 添加关闭钩子
   *
   * @param runnable 任务
   * @return Thread
   */
  public Thread addShutdownHook(Runnable runnable) {
    Thread thread = defaultThreadFactory().newThread(runnable);
    Runtime.getRuntime().addShutdownHook(thread);
    return thread;
  }

  /**
   * 全局pool
   *
   * thread_pool.global.pool_size=10
   * <br>
   * thread_pool.global.queue_size=100000
   *
   * @return pool
   */
  public synchronized SecurityThreadPoolExecutor globalPool() {
    if (globalPool == null) {
      int poolSize = Integer
          .parseInt(ConfigFactory.getProperty("thread_pool.global.pool_size", "10"));
      int queueSize = Integer
          .parseInt(ConfigFactory.getProperty("thread_pool.global.queue_size", "100000"));
      globalPool = newThreadPool(poolSize, poolSize, 120, queueSize, "global-pool-%d");
      globalPool.setSecurityChecker(
          new SecurityChecker().ownerThread(addShutdownHook(globalPool::shutdown)));
    }
    return globalPool;
  }

  /**
   * 全局时间轮，通过以下参数可进行配置，默认为毫秒级时间轮
   *
   * wheel_timer.global.duration=1
   * <br>
   * wheel_timer.global.wheel_size=60
   * <br>
   * wheel_timer.global.pool_size=10
   *
   * @return WheelTimer
   */
  public synchronized WheelTimer globalTimer() {
    if (globalTimer == null) {
      globalTimer = new WheelTimer(
          Integer.parseInt(ConfigFactory.getProperty("wheel_timer.global.duration", "1")),
          Integer.parseInt(ConfigFactory.getProperty("wheel_timer.global.wheel_size", "60")),
          Integer.parseInt(ConfigFactory.getProperty("wheel_timer.global.pool_size", "10")));
      globalTimer.getSecurityChecker()
          .ownerThread(ThreadUtils.addShutdownHook(globalTimer::shutdown));
    }
    return globalTimer;
  }
}
