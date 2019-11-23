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

package vip.justlive.oxygen.core.net.aio.core;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 抽象worker
 *
 * @author wubo
 */
public abstract class AbstractWorker<T> implements Runnable {

  final ChannelContext channelContext;
  LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
  volatile boolean stopped;

  private Lock lock = new ReentrantLock(true);
  private int maxLoop = 10;
  private int executeCount = 0;

  AbstractWorker(ChannelContext channelContext) {
    this.channelContext = channelContext;
  }

  /**
   * 添加待处理的数据
   *
   * @param data 数据
   * @return 是否添加成功
   */
  protected boolean add(T data) {
    return queue.offer(data);
  }

  /**
   * 执行任务
   */
  public synchronized void execute() {
    if (!stopped && executeCount++ % maxLoop == 0) {
      channelContext.getGroupContext().getWorkerExecutor().execute(this);
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
    lock.lock();
    try {
      for (int i = 0; i < maxLoop; i++) {
        loopRun();
      }
    } finally {
      executeCount = Math.max(executeCount - maxLoop, 0);
      lock.unlock();
    }
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
