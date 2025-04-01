/*
 * Copyright (C) 2025 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.CoreConfigKeys;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.util.timer.WheelTimer;

/**
 * 线程池监控
 *
 * @author wubo
 */
@Slf4j
public class ThreadPoolMonitor implements Plugin {

  private static final List<Executor> POOLS = new CopyOnWriteArrayList<>();
  private static final List<WheelTimer> TIMERS = new CopyOnWriteArrayList<>();
  private static final List<Object> USER_POOLS = new CopyOnWriteArrayList<>();

  private static final AtomicReference<PoolMonitorConsumer<Executor>> EXECUTOR_CONSUMER = new AtomicReference<>(
      ThreadPoolMonitor::monitor);
  private static final AtomicReference<PoolMonitorConsumer<Object>> USER_POOL_CONSUMER = new AtomicReference<>(
      ThreadPoolMonitor::monitorUserPool);
  private static final AtomicReference<PoolMonitorConsumer<WheelTimer>> TIMER_CONSUMER = new AtomicReference<>(
      ThreadPoolMonitor::monitorWheelTimer);

  private ScheduledFuture<Void> future;

  /**
   * 添加线程池
   *
   * @param pool 线程池
   */
  public static void add(Executor pool) {
    if (!POOLS.contains(pool)) {
      POOLS.add(pool);
    }
  }

  /**
   * 添加用户线程池，用于自定义实现的线程池类型
   *
   * @param pool 线程池
   */
  public static void addUserPool(Object pool) {
    if (!USER_POOLS.contains(pool)) {
      USER_POOLS.add(pool);
    }
  }

  /**
   * 添加时间轮
   *
   * @param timer 时间轮
   */
  public static void add(WheelTimer timer) {
    if (!TIMERS.contains(timer)) {
      TIMERS.add(timer);
    }
  }

  /**
   * 删除线程池监控
   *
   * @param pool 线程池
   */
  public static void remove(Executor pool) {
    log.info("remove pool monitor : {}", pool);
    POOLS.remove(pool);
  }

  /**
   * 删除用户线程池监控
   *
   * @param pool 线程池
   */
  public static void removeUserPool(Object pool) {
    log.info("remove user pool monitor : {}", pool);
    USER_POOLS.remove(pool);
  }

  /**
   * 删除时间轮监控
   *
   * @param timer 时间轮
   */
  public static void remove(WheelTimer timer) {
    log.info("remove timer monitor : {}", timer);
    TIMERS.remove(timer);
  }

  /**
   * 设置线程池监控处理
   *
   * @param consumer 处理器
   */
  public static void setExecutorConsumer(PoolMonitorConsumer<Executor> consumer) {
    Objects.requireNonNull(consumer, "consumer can't be null");
    EXECUTOR_CONSUMER.set(consumer);
  }

  /**
   * 设置用户线程池监控处理
   *
   * @param consumer 处理器
   */
  public static void setUserPoolConsumer(PoolMonitorConsumer<Object> consumer) {
    Objects.requireNonNull(consumer, "consumer can't be null");
    USER_POOL_CONSUMER.set(consumer);
  }

  @Override
  public void start() {
    boolean b = CoreConfigKeys.THREAD_POOL_MONITOR_ENABLED.castValue(boolean.class);
    log.info("start thread pool monitor, enabled={}", b);
    if (b) {
      future = ThreadUtils.globalTimer().scheduleWithFixedDelay(this::monitor, 30,
          CoreConfigKeys.THREAD_POOL_MONITOR_INTERVAL.castValue(long.class), TimeUnit.SECONDS);
    }
  }

  @Override
  public void stop() {
    log.info("stop thread pool monitor");
    if (CoreConfigKeys.THREAD_POOL_MONITOR_ENABLED.castValue(boolean.class) && future != null) {
      future.cancel(true);
    }
    POOLS.clear();
    TIMERS.clear();
    USER_POOLS.clear();
  }

  private void monitor() {
    log.info("report pool status begin.");
    log.info("summary: Executor total size: {}, WheelTimer total size: {}, UserPool total size: {}",
        POOLS.size(), TIMERS.size(), USER_POOLS.size());

    List<Executor> shutdownExecutors = new ArrayList<>();
    for (Executor pool : POOLS) {
      if (EXECUTOR_CONSUMER.get().monitor(pool)) {
        log.info("Executor [{}] is shutdown, this pool will remove from monitor list",
            Integer.toHexString(pool.hashCode()));
        shutdownExecutors.add(pool);
      }
    }
    POOLS.removeAll(shutdownExecutors);

    List<Object> shutdownUserPools = new ArrayList<>();
    for (Object pool : USER_POOLS) {
      if (USER_POOL_CONSUMER.get().monitor(pool)) {
        log.info("UserPool [{}] is shutdown, this pool will remove from monitor list",
            Integer.toHexString(pool.hashCode()));
        shutdownUserPools.add(pool);
      }
    }
    USER_POOLS.removeAll(shutdownUserPools);

    List<WheelTimer> shutdownWheelTimers = new ArrayList<>();
    for (WheelTimer timer : TIMERS) {
      if (TIMER_CONSUMER.get().monitor(timer)) {
        log.info("WheelTimer [{}] is shutdown, this timer will remove from monitor list",
            Integer.toHexString(timer.hashCode()));
        shutdownWheelTimers.add(timer);
      }
    }
    TIMERS.removeAll(shutdownWheelTimers);

    log.info("report pool status end.");
  }

  private static boolean monitor(Executor pool) {
    log.info("Executor status : {}", pool);
    return pool instanceof ThreadPoolExecutor && ((ThreadPoolExecutor) pool).isTerminated();
  }

  private static boolean monitorUserPool(Object pool) {
    log.info("UserPool status : {}", pool);
    return false;
  }

  private static boolean monitorWheelTimer(WheelTimer timer) {
    log.info("WheelTimer status : {}", timer);
    return timer.isShutdown();
  }


  /**
   * 线程池监控消费处理
   *
   * @param <T> 泛型
   */
  public interface PoolMonitorConsumer<T> {


    /**
     * 监控线程池
     *
     * @param pool 线程池
     * @return true表示当前线程池已关闭，需要移除监控
     */
    boolean monitor(T pool);
  }
}
