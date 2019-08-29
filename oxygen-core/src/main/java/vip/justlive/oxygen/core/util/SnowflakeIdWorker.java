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

import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 雪花算法id
 * <br>
 * SnowFlake的结构如下(每部分用-分开):
 * <p>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * <p>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0
 * <p>
 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截) 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IdWorker类的startTime属性）。41位的时间截，可以使用69年，年
 * {@code  T * = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69 }
 * <p>
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId
 * <p>
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号
 * <p>
 * 加起来刚好64位，为一个Long型。<br> SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 *
 * @author wubo
 */
public class SnowflakeIdWorker {

  /**
   * 起始时间戳，用于用当前时间戳减去这个时间戳，算出偏移量
   */
  private static final long START_TIME = 1519740777809L;
  /**
   * workerId占用的位数5（表示只允许workId的范围为：0-1023）
   */
  private static final long WORKERID_BITS = 5L;
  /**
   * dataCenterId占用的位数：5
   */
  private static final long DATACENTERID_BITS = 5L;
  /**
   * 序列号占用的位数：12（表示只允许workId的范围为：0-4095）
   */
  private static final long SEQUENCE_BITS = 12L;
  /**
   * workerId可以使用的最大数值：31
   */
  private static final long MAX_WORKERID = ~(-1L << WORKERID_BITS);
  /**
   * dataCenterId可以使用的最大数值：31
   */
  private static final long MAX_DATACENTERID = ~(-1L << DATACENTERID_BITS);

  private static final long WORKERID_SHIFT = SEQUENCE_BITS;
  private static final long DATACENTERID_SHIFT = SEQUENCE_BITS + WORKERID_BITS;
  private static final long TIMESTAMP_LEFT_SHIFT =
      SEQUENCE_BITS + WORKERID_BITS + DATACENTERID_BITS;

  /**
   * 用mask防止溢出:位与运算保证计算的结果范围始终是 0-4095
   */
  private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

  private static final int MAX_OFFSET = 5;

  private long workerId;
  private long dataCenterId;
  private long sequence = 0L;
  private long lastTimestamp = -1L;

  /**
   * 基于Snowflake创建分布式ID生成器
   * <p>
   * 注：sequence
   *
   * @param workerId 工作机器ID,数据范围为0~31
   * @param dataCenterId 数据中心ID,数据范围为0~31
   */
  public SnowflakeIdWorker(long workerId, long dataCenterId) {
    if (workerId > MAX_WORKERID || workerId < 0) {
      throw new IllegalArgumentException(
          String.format("worker Id can't be greater than %d or less than 0", MAX_WORKERID));
    }
    if (dataCenterId > MAX_DATACENTERID || dataCenterId < 0) {
      throw new IllegalArgumentException(
          String.format("dataCenter Id can't be greater than %d or less than 0", MAX_DATACENTERID));
    }

    this.workerId = workerId;
    this.dataCenterId = dataCenterId;
  }

  /**
   * 获取id 使用默认worker
   *
   * @return id
   */
  public static long defaultNextId() {
    return InstanceHolder.INSTANCE.nextId();
  }

  /**
   * 获取id
   *
   * @return id
   */
  public synchronized Long nextId() {
    long timestamp = this.timeGen();

    // 闰秒：如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
    if (timestamp < lastTimestamp) {
      long offset = lastTimestamp - timestamp;
      if (offset <= MAX_OFFSET) {
        try {
          this.wait(offset << 1);
          timestamp = this.timeGen();
          if (timestamp < lastTimestamp) {
            throw new IllegalStateException(String
                .format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                    offset));
          }
        } catch (Exception e) {
          throw Exceptions.wrap(e);
        }
      } else {
        throw new IllegalStateException(String
            .format("Clock moved backwards.  Refusing to generate id for %d milliseconds", offset));
      }
    }

    // 解决跨毫秒生成ID序列号始终为偶数的缺陷:如果是同一时间生成的，则进行毫秒内序列
    if (lastTimestamp == timestamp) {
      // 通过位与运算保证计算的结果范围始终是 0-4095
      sequence = (sequence + 1) & SEQUENCE_MASK;
      if (sequence == 0) {
        timestamp = this.tilNextMillis(lastTimestamp);
      }
    } else {
      // 时间戳改变，毫秒内序列重置
      sequence = 0L;
    }

    lastTimestamp = timestamp;

    /*
     * 1.左移运算是为了将数值移动到对应的段(41、5、5，12那段因为本来就在最右，因此不用左移)
     * 2.然后对每个左移后的值(la、lb、lc、sequence)做位或运算，是为了把各个短的数据合并起来，合并成一个二进制数 3.最后转换成10进制，就是最终生成的id
     */
    return ((timestamp - START_TIME) << TIMESTAMP_LEFT_SHIFT) | (dataCenterId << DATACENTERID_SHIFT)
        | (workerId << WORKERID_SHIFT) | sequence;
  }

  /**
   * 保证返回的毫秒数在参数之后(阻塞到下一个毫秒，直到获得新的时间戳)
   *
   * @param lastTimestamp 上一毫秒值
   * @return timestamp
   */
  private long tilNextMillis(long lastTimestamp) {
    long timestamp = this.timeGen();
    while (timestamp <= lastTimestamp) {
      timestamp = this.timeGen();
    }
    return timestamp;
  }

  /**
   * 获得系统当前毫秒数
   *
   * @return timestamp
   */
  private long timeGen() {
    return System.currentTimeMillis();
  }

  private static class InstanceHolder {

    static final SnowflakeIdWorker INSTANCE = new SnowflakeIdWorker(0, 0);

    private InstanceHolder() {
    }
  }
}
