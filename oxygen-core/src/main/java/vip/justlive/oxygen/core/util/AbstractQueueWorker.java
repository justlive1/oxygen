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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 使用queue异步化任务执行器
 *
 * @author wubo
 */
public abstract class AbstractQueueWorker<T> implements Runnable {

  private static final int MAX_LOOP = 10;

  protected final LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
  protected final Executor executor;
  protected volatile boolean stopped;
  private volatile boolean running;

  protected AbstractQueueWorker(Executor executor) {
    this.executor = executor;
  }

  /**
   * 添加待处理的数据
   *
   * @param data 数据
   * @return 是否添加成功
   */
  public boolean add(T data) {
    if (stopped) {
      return false;
    }
    return queue.offer(data);
  }

  /**
   * 添加数据并触发任务
   *
   * @param data 数据
   */
  public void addThenExecute(T data) {
    if (add(data)) {
      execute();
    }
  }

  /**
   * 执行任务
   */
  public synchronized void execute() {
    if (stopped) {
      return;
    }
    if (!running) {
      running = true;
      executor.execute(this);
    }
  }

  /**
   * 处理逻辑
   *
   * @param data 数据
   */
  public abstract void handle(List<T> data);

  /**
   * 开启
   */
  public void start() {
    stopped = false;
  }

  /**
   * 关闭
   */
  public void stop() {
    stopped = true;
    queue.clear();
  }

  @Override
  public void run() {
    try {
      for (int i = 0; i < MAX_LOOP; i++) {
        loopRun();
      }
    } finally {
      running = false;
    }
    if (!queue.isEmpty()) {
      execute();
    }
  }

  public void clear() {
    queue.clear();
  }

  private void loopRun() {
    if (stopped) {
      return;
    }
    List<T> data = new LinkedList<>();
    queue.drainTo(data);
    if (data.isEmpty()) {
      return;
    }
    handle(data);
  }
}
