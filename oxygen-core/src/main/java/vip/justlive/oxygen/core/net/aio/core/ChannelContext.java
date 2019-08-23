/*
 * Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */

package vip.justlive.oxygen.core.net.aio.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import lombok.Data;
import lombok.experimental.Accessors;
import vip.justlive.oxygen.core.util.SnowflakeIdWorker;

/**
 * channel上下文
 *
 * @author wubo
 */
@Data
@Accessors(chain = true)
public class ChannelContext {

  private final long id;
  private final boolean server;
  private final GroupContext groupContext;
  private final ReadWorker readWorker;
  private final ReadHandler readHandler;
  private final WriteWorker writeWorker;
  private final WriteHandler writeHandler;

  private AsynchronousSocketChannel channel;
  private InetSocketAddress address;

  private volatile boolean closed;

  // stat

  private long createAt = System.currentTimeMillis();
  private long lastReceivedAt = -1;
  private long lastSentAt = -1;
  private int retryAttempts = 0;

  public ChannelContext(GroupContext groupContext, AsynchronousSocketChannel channel) {
    this(groupContext, channel, true);
  }

  public ChannelContext(GroupContext groupContext, AsynchronousSocketChannel channel,
      boolean server) {
    this(SnowflakeIdWorker.defaultNextId(), groupContext, channel, server);
  }

  public ChannelContext(long id, GroupContext groupContext, AsynchronousSocketChannel channel,
      boolean server) {
    this.id = id;
    this.server = server;
    this.groupContext = groupContext;
    this.readWorker = new ReadWorker(this);
    this.readHandler = new ReadHandler(this);
    this.writeWorker = new WriteWorker(this);
    this.writeHandler = new WriteHandler(this);
    setChannel(channel);
  }

  public void setChannel(AsynchronousSocketChannel channel) {
    this.channel = channel;
    try {
      if (isServer()) {
        this.address = (InetSocketAddress) channel.getRemoteAddress();
      } else {
        this.address = (InetSocketAddress) channel.getLocalAddress();
      }
    } catch (IOException e) {
      //ignore
    }
  }

  public void write(Object data) {
    if (closed) {
      return;
    }
    writeWorker.add(data);
    writeWorker.execute();
  }

  void read(ByteBuffer buffer) {
    if (closed) {
      return;
    }
    ByteBuffer ret = ByteBuffer.allocate(buffer.limit() - buffer.position());
    ret.put(buffer);
    ret.flip();

    readWorker.add(ret);
    readWorker.execute();
  }

  public void start() {
    closed = false;
    readWorker.start();
    writeWorker.start();
    retryAttempts = 0;
    groupContext.bind(this);
  }

  public void close() {
    if (closed) {
      return;
    }
    closed = true;
    groupContext.unbind(this);
    try {
      if (groupContext.getAioListener() != null) {
        groupContext.getAioListener().onClosed(this);
      }
    } finally {
      readWorker.stop();
      writeWorker.stop();
      Utils.close(channel);
      readWorker.queue.clear();
      writeWorker.queue.clear();
    }
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ChannelContext) {
      ChannelContext other = (ChannelContext) obj;
      return id == other.id && channel == other.channel;
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("[%s:%s]", id, address);
  }
}
