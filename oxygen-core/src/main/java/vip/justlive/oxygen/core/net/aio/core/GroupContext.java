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

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import vip.justlive.oxygen.core.util.ThreadUtils;

/**
 * 聚合上下文
 *
 * @author wubo
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class GroupContext {

  /**
   * aio处理逻辑
   */
  private final AioHandler aioHandler;
  /**
   * aio监听
   */
  private AioListener aioListener;

  /**
   * 服务是否已停止
   */
  private volatile boolean stopped;
  /**
   * buffer大小
   */
  private int bufferCapacity = 8 * 1024;
  /**
   * 服务端地址
   */
  private InetSocketAddress serverAddress;
  /**
   * 客户端心跳发送间隔
   */
  private long beatInterval = 5000L;
  /**
   * 客户端是否开启重连
   */
  private boolean retryEnabled = false;
  /**
   * 客户端重连间隔
   */
  private long retryInterval = 5000L;
  /**
   * 客户端最大重连次数， 0或负数一直重连
   */
  private int retryMaxAttempts = 0;

  /**
   * 连接线程数
   */
  private int acceptThreads = 100;
  /**
   * 连接线程最大等待数
   */
  private int acceptMaxWaiter = 10000;
  /**
   * 工作线程数
   */
  private int workerThreads = 200;
  /**
   * 工作线程最大等待数
   */
  private int workerMaxWaiter = 1000000;
  /**
   * 是否hold住端口，true的话随主线程退出而退出，false的话则要主动退出
   */
  private boolean daemon = false;

  private ThreadPoolExecutor groupExecutor;
  private ThreadPoolExecutor workerExecutor;
  private ScheduledExecutorService scheduledExecutor;

  private AsynchronousChannelGroup channelGroup;

  /**
   * 在线channel
   */
  private Map<Long, ChannelContext> channels = new ConcurrentHashMap<>(16, 0.5f);

  /**
   * 关闭
   */
  public void close() {
    stopped = true;
    channels.clear();
    if (channelGroup != null) {
      channelGroup.shutdown();
    }
    groupExecutor.shutdown();
    if (workerExecutor != null) {
      workerExecutor.shutdown();
    }
    if (scheduledExecutor != null) {
      scheduledExecutor.shutdown();
    }
  }

  public ThreadPoolExecutor getGroupExecutor() {
    if (groupExecutor == null) {
      groupExecutor = ThreadUtils
          .newThreadPool(acceptThreads, acceptThreads, 120, acceptMaxWaiter, "aio-server-%d",
              daemon);
      groupExecutor.prestartCoreThread();
    }
    return groupExecutor;
  }

  public ThreadPoolExecutor getWorkerExecutor() {
    if (workerExecutor == null) {
      workerExecutor = ThreadUtils
          .newThreadPool(workerThreads, workerThreads, 120, workerMaxWaiter, "aio-worker-%d");
      workerExecutor.prestartCoreThread();
    }
    return workerExecutor;
  }

  public ScheduledExecutorService getScheduledExecutor() {
    if (scheduledExecutor == null) {
      scheduledExecutor = ThreadUtils.newScheduledExecutor(5, "aio-scheduled-%d");
    }
    return scheduledExecutor;
  }

  /**
   * 绑定channel
   *
   * @param channelContext channel上下文
   */
  public void bind(ChannelContext channelContext) {
    if (channelContext != null) {
      this.channels.put(channelContext.getId(), channelContext);
    }
  }

  /**
   * 解绑channel
   *
   * @param channelContext channel上下文
   */
  public void unbind(ChannelContext channelContext) {
    if (channelContext != null) {
      this.channels.remove(channelContext.getId());
    }
  }

}
