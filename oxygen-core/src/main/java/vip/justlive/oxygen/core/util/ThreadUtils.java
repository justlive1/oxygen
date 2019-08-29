/*
 * Copyright (C) 2019 the original author or authors.
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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;

/**
 * 线程工具类
 *
 * @author wubo
 */
@UtilityClass
public class ThreadUtils {

  private static final ThreadLocal<Map<String, Object>> LOCAL = ThreadLocal
      .withInitial(ConcurrentHashMap::new);

  private static final ThreadPoolExecutor CACHED_POOL = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
      60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
      new ThreadFactoryBuilder().setDaemon(true).setNameFormat("cached-pool-%d").build());


  /**
   * 线程存储键值
   *
   * @param key 键
   * @param value 值
   */
  public static void putVal(String key, Object value) {
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
  public static <T> T getVal(String key) {
    return (T) LOCAL.get().get(key);
  }

  /**
   * 清除线程存储值
   */
  public static void clear() {
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
  public static ThreadPoolExecutor newThreadPool(int corePoolSize, int maxPoolSize,
      int keepAliveSeconds, int queueCapacity, String nameFormat) {
    return newThreadPool(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, nameFormat,
        new ThreadPoolExecutor.AbortPolicy());
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
  public static ThreadPoolExecutor newThreadPool(int corePoolSize, int maxPoolSize,
      int keepAliveSeconds, int queueCapacity, String nameFormat,
      RejectedExecutionHandler handler) {
    return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(queueCapacity),
        new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(true).build(), handler);
  }

  /**
   * 构造定时任务池
   *
   * @param corePoolSize 线程数
   * @param nameFormat 线程名称format
   * @return 任务池
   */
  public static ScheduledExecutorService newScheduledExecutor(int corePoolSize, String nameFormat) {
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
  public static ScheduledExecutorService newScheduledExecutor(int corePoolSize, String nameFormat,
      RejectedExecutionHandler handler) {
    return new ScheduledThreadPoolExecutor(corePoolSize,
        new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(true).build(), handler);
  }

  /**
   * 线程sleep等待
   *
   * @param millis 毫秒值
   */
  public static void sleep(long millis) {
    sleep(millis, TimeUnit.MILLISECONDS);
  }

  /**
   * 线程sleep等待
   *
   * @param time 时间
   * @param unit 单位
   */
  public static void sleep(long time, TimeUnit unit) {
    try {
      unit.sleep(time);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * 返回cached pool
   *
   * @return pool
   */
  public static ThreadPoolExecutor cachedPool() {
    return CACHED_POOL;
  }

}
