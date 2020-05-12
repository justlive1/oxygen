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

package vip.justlive.oxygen.core.util.timer;

import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.LongUnaryOperator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.CronExpression;
import vip.justlive.oxygen.core.util.RepeatRunnable;
import vip.justlive.oxygen.core.util.SecurityChecker;
import vip.justlive.oxygen.core.util.SecurityThreadPoolExecutor;
import vip.justlive.oxygen.core.util.SecurityThreadPoolExecutor.PoolQueue;
import vip.justlive.oxygen.core.util.ThreadFactoryBuilder;

/**
 * 时间轮
 *
 * @author wubo
 */
@Slf4j
public class WheelTimer {

  private static final int STATE_INIT = 0;
  private static final int STATE_STARTED = 1;
  private static final int STATE_SHUTDOWN = 2;
  private static final long POLL_TIMEOUT;
  private static final ThreadFactory FACTORY;
  private static final AtomicInteger COUNT = new AtomicInteger();

  static {
    POLL_TIMEOUT = Long.parseLong(ConfigFactory.getProperty("wheel_timer.poll.timeout", "100"));
    FACTORY = new ThreadFactoryBuilder().setDaemon(true).setPriority(Thread.NORM_PRIORITY)
        .setNameFormat("wheel-task-%d").build();
  }

  private final long duration;
  private final int wheelSize;
  private final ThreadPoolExecutor executor;
  @Getter
  private final SecurityChecker securityChecker = new SecurityChecker();
  private final AtomicInteger state = new AtomicInteger(STATE_INIT);
  private final DelayQueue<Slot> delayQueue = new DelayQueue<>();
  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private Wheel wheel;
  private final RepeatRunnable worker = new RepeatRunnable("wheel-timer-" + COUNT.getAndIncrement(),
      this::doWork);

  public WheelTimer(long duration, int wheelSize) {
    this(duration, wheelSize, 1);
  }

  public WheelTimer(long duration, int wheelSize, int taskPoolSize) {
    this(duration, wheelSize, taskPoolSize, FACTORY);
  }

  public WheelTimer(long duration, int wheelSize, int taskPoolSize, ThreadFactory factory) {
    if (taskPoolSize < 1) {
      throw new IllegalArgumentException("taskPoolSize must be greater than 0: " + taskPoolSize);
    }
    this.duration = duration;
    this.wheelSize = wheelSize;
    PoolQueue queue = new PoolQueue();
    SecurityThreadPoolExecutor pool = new SecurityThreadPoolExecutor(1, taskPoolSize, 120,
        TimeUnit.SECONDS, queue, factory);
    queue.setPool(pool);
    this.executor = pool;
  }

  /**
   * 启动时间轮，添加任务时会自动执行
   */
  public void start() {
    int stateValue = state.get();
    if (stateValue != STATE_INIT && stateValue != STATE_STARTED) {
      throw new IllegalStateException("WheelTimer.state is illegal");
    }
    if (!state.compareAndSet(STATE_INIT, STATE_STARTED)) {
      return;
    }
    FACTORY.newThread(worker).start();
    worker.awaitRunning();
    wheel = new Wheel(duration, wheelSize, System.currentTimeMillis(), delayQueue);
    log.info("WheelTimer started");
  }

  /**
   * 关闭时间轮
   */
  public void shutdown() {
    securityChecker.checkPermission();
    if (!state.compareAndSet(STATE_STARTED, STATE_SHUTDOWN) && !state
        .compareAndSet(STATE_INIT, STATE_SHUTDOWN) && state.get() != STATE_SHUTDOWN) {
      throw new IllegalStateException("WheelTimer.state is illegal");
    }
    executor.shutdown();
    worker.shutdown();
    delayQueue.forEach(slot -> {
      Task<?> task = slot.head;
      while (task != null) {
        task = slot.remove(task);
      }
    });
    delayQueue.clear();
    log.info("WheelTimer shutdown");
  }

  /**
   * 是否关闭
   *
   * @return true为关闭
   */
  public boolean isShutdown() {
    return state.get() == STATE_SHUTDOWN;
  }

  /**
   * 延迟任务
   *
   * @param callable 任务
   * @param delay 延迟
   * @param unit 时间单位
   * @param <T> 泛型
   * @return ScheduledFuture
   */
  public <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit unit) {
    long deadline = check(delay, unit);
    Task<T> task = new Task<>(deadline, callable);
    addTaskInLock(task);
    return task;
  }

  /**
   * 延迟任务
   *
   * @param command 任务
   * @param delay 延迟
   * @param unit 时间单位
   * @return ScheduledFuture
   */
  public ScheduledFuture<Void> schedule(Runnable command, long delay, TimeUnit unit) {
    long deadline = check(delay, unit);
    Task<Void> task = new Task<>(deadline, command);
    addTaskInLock(task);
    return task;
  }

  /**
   * 固定周期任务
   *
   * @param command 任务
   * @param initialDelay 初始延迟
   * @param period 周期
   * @param unit 时间单位
   * @return ScheduledFuture
   */
  public ScheduledFuture<Void> scheduleAtFixedRate(Runnable command, long initialDelay, long period,
      TimeUnit unit) {
    return scheduleWithDelay(command, initialDelay, unit, d -> d + unit.toMillis(period));
  }

  /**
   * 固定延迟任务
   *
   * @param command 任务
   * @param initialDelay 初始延迟
   * @param delay 延迟
   * @param unit 时间单位
   * @return ScheduledFuture
   */
  public ScheduledFuture<Void> scheduleWithFixedDelay(Runnable command, long initialDelay,
      long delay, TimeUnit unit) {
    return scheduleWithDelay(command, initialDelay, unit,
        d -> System.currentTimeMillis() + unit.toMillis(delay));
  }

  /**
   * 自定义周期任务
   *
   * @param command 任务
   * @param initialDelay 初始延迟
   * @param unit 时间周期
   * @param operator 获取周期函数, 返回Long.MIN_VALUE可终止任务
   * @return ScheduledFuture
   */
  public ScheduledFuture<Void> scheduleWithDelay(Runnable command, long initialDelay, TimeUnit unit,
      LongUnaryOperator operator) {
    long deadline = check(initialDelay, unit);
    PeriodTask task = new PeriodTask(deadline, command, operator, this);
    addTaskInLock(task);
    return task;
  }

  /**
   * cron任务
   *
   * @param command 任务
   * @param cron 表达式
   * @return ScheduledFuture
   */
  public ScheduledFuture<Void> scheduleOnCron(Runnable command, String cron) {
    start();
    LongUnaryOperator operator = new CronExpression(cron).operator();
    long deadline = operator.applyAsLong(0);
    if (deadline == Long.MIN_VALUE) {
      throw Exceptions.fail("cron doesn't have any match in the future");
    }
    PeriodTask task = new PeriodTask(deadline, command, operator, this);
    addTaskInLock(task);
    return task;
  }

  void addTask(Task<?> task) {
    if (!wheel.add(task) && !task.isCancelled()) {
      executor.execute(task);
    }
  }

  private void doWork() {
    Slot slot = null;
    try {
      slot = delayQueue.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    if (slot == null) {
      return;
    }
    readWriteLock.writeLock().lock();
    try {
      while (slot != null) {
        wheel.advanceClock(slot.getDeadline());
        Task<?> task = slot.head;
        while (task != null) {
          Task<?> next = slot.remove(task);
          if (!task.isCancelled()) {
            addTask(task);
          }
          task = next;
        }
        slot = delayQueue.poll();
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  private void addTaskInLock(Task<?> task) {
    readWriteLock.readLock().lock();
    try {
      addTask(task);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  private long check(long delay, TimeUnit unit) {
    start();
    long deadline = System.currentTimeMillis() + unit.toMillis(delay);
    if (delay > 0 && deadline < 0) {
      deadline = Long.MAX_VALUE;
    }
    return deadline;
  }

}
