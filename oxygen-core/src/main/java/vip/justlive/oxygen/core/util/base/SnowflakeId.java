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
package vip.justlive.oxygen.core.util.base;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 雪花算法id
 *
 * @author wubo
 */
public class SnowflakeId {

  private static final long START_TIME = 1570000000000L;
  private static final long WORKER_ID_BITS = 5L;
  private static final long DATA_CENTER_ID_BITS = 5L;
  private static final long SEQUENCE_BITS = 12L;
  private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
  private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
  private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
  private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
  private static final long TIMESTAMP_LEFT_SHIFT =
      SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;
  private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

  private final long workerId;
  private final long dataCenterId;
  private long sequence = 0L;
  private long lastTimestamp = -1L;

  /**
   * 基于Snowflake创建分布式ID生成器
   *
   * @param workerId 工作机器ID,数据范围为0~31
   * @param dataCenterId 数据中心ID,数据范围为0~31
   */
  public SnowflakeId(long workerId, long dataCenterId) {
    if (workerId > MAX_WORKER_ID || workerId < 0) {
      throw new IllegalArgumentException(
          String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
    }
    if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
      throw new IllegalArgumentException(
          String
              .format("dataCenter Id can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
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
   * 根据生成的ID，获取生成时间
   *
   * @param id 算法生成的id
   * @return 生成的时间
   */
  public static long getCreatedAt(long id) {
    return (id >> TIMESTAMP_LEFT_SHIFT & ~(-1L << 41L)) + START_TIME;
  }

  /**
   * 根据生成的ID，获取机器id
   *
   * @param id 算法生成的id
   * @return 所属机器的id
   */
  public static long getWorkerId(long id) {
    return id >> WORKER_ID_SHIFT & MAX_WORKER_ID;
  }

  /**
   * 根据生成的ID，获取数据中心id
   *
   * @param id 算法生成的id
   * @return 所属数据中心
   */
  public static long getDataCenterId(long id) {
    return id >> DATA_CENTER_ID_SHIFT & MAX_DATA_CENTER_ID;
  }

  /**
   * 获取id0
   *
   * @return id
   */
  public synchronized Long nextId() {
    long timestamp = this.timeGen();

    // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
    if (timestamp < lastTimestamp) {
      throw new IllegalArgumentException(String
          .format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
              lastTimestamp - timestamp));
    }

    // 如果是同一时间生成的，则进行毫秒内序列
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
    return ((timestamp - START_TIME) << TIMESTAMP_LEFT_SHIFT) | (dataCenterId
        << DATA_CENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
  }

  private long tilNextMillis(long lastTimestamp) {
    long timestamp = this.timeGen();
    while (timestamp <= lastTimestamp) {
      timestamp = this.timeGen();
    }
    return timestamp;
  }

  private long timeGen() {
    return System.currentTimeMillis();
  }

  private static class InstanceHolder {

    static final SnowflakeId INSTANCE = new SnowflakeId(0,
        ThreadLocalRandom.current().nextLong(MAX_DATA_CENTER_ID));

    private InstanceHolder() {
    }
  }

}
