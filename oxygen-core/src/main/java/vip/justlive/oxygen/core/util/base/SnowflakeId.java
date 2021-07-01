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
import lombok.Data;
import lombok.experimental.Accessors;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

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

  private final long baseTime;
  private final long workerId;
  private final long dataCenterId;

  private final long sequenceMask;
  private final long maxWorkerId;
  private final long maxDataCenterId;
  private final long workerIdShift;
  private final long dataCenterIdShift;
  private final long timestampLeftShift;

  private long sequence = 0L;
  private long lastTimestamp = -1L;

  /**
   * 基于Snowflake创建分布式ID生成器
   *
   * @param workerId     工作机器ID,数据范围为0~31
   * @param dataCenterId 数据中心ID,数据范围为0~31
   */
  public SnowflakeId(long workerId, long dataCenterId) {
    this(new Cfg().setWorkerId(workerId).setDataCenterId(dataCenterId));
  }

  /**
   * 基于Snowflake创建分布式ID生成器
   *
   * @param cfg 配置
   */
  public SnowflakeId(Cfg cfg) {
    long workerIdBits = cfg.workerIdBits < 0 ? WORKER_ID_BITS : cfg.workerIdBits;
    long dataCenterIdBits = cfg.dataCenterIdBits < 0 ? DATA_CENTER_ID_BITS : cfg.dataCenterIdBits;
    this.maxWorkerId = ~(-1L << workerIdBits);
    this.maxDataCenterId = ~(-1L << dataCenterIdBits);
    if (cfg.workerId > maxWorkerId || cfg.workerId < 0) {
      throw new IllegalArgumentException(
          String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
    }
    if (cfg.dataCenterId > maxDataCenterId || cfg.dataCenterId < 0) {
      throw new IllegalArgumentException(
          String
              .format("dataCenter Id can't be greater than %d or less than 0", maxDataCenterId));
    }
    long sequenceBits = cfg.sequenceBits < 0 ? SEQUENCE_BITS : cfg.sequenceBits;
    this.sequenceMask = ~(-1L << sequenceBits);
    this.workerIdShift = sequenceBits;
    this.dataCenterIdShift = sequenceBits + workerIdBits;
    this.timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
    this.workerId = cfg.workerId;
    this.dataCenterId = cfg.dataCenterId;
    this.baseTime = cfg.baseTime > 0 ? cfg.baseTime : START_TIME;
  }


  /**
   * 获取id 使用默认worker
   *
   * @return id
   */
  public static long defaultNextId() {
    return defaultInstance().nextId();
  }

  /**
   * 获取默认实例
   *
   * @return snowflake
   */
  public static SnowflakeId defaultInstance() {
    return InstanceHolder.INSTANCE;
  }


  /**
   * 根据生成的ID，获取生成时间
   *
   * @param id 算法生成的id
   * @return 生成的时间
   */
  public long getCreatedAt(long id) {
    return (id >> timestampLeftShift & ~(-1L << 41L)) + baseTime;
  }

  /**
   * 根据生成的ID，获取机器id
   *
   * @param id 算法生成的id
   * @return 所属机器的id
   */
  public long getWorkerId(long id) {
    return id >> workerIdShift & maxWorkerId;
  }

  /**
   * 根据生成的ID，获取数据中心id
   *
   * @param id 算法生成的id
   * @return 所属数据中心
   */
  public long getDataCenterId(long id) {
    return id >> dataCenterIdShift & maxDataCenterId;
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
          .format("Clock moved backwards.Refusing to generate id for %d milliseconds",
              lastTimestamp - timestamp));
    }

    // 如果是同一时间生成的，则进行毫秒内序列
    if (lastTimestamp == timestamp) {
      // 通过位与运算保证计算的结果范围始终是 [0, -1L^(-1L<<sequenceBits)]
      sequence = (sequence + 1) & sequenceMask;
      if (sequence == 0) {
        timestamp = this.tilNextMillis(lastTimestamp);
      }
    } else {
      // 时间戳改变，毫秒内序列重置
      sequence = 0L;
    }

    lastTimestamp = timestamp;
    return ((timestamp - baseTime) << timestampLeftShift) | (dataCenterId << dataCenterIdShift) | (
        workerId << workerIdShift) | sequence;
  }

  private long tilNextMillis(long lastTimestamp) {
    while (true) {
      long timestamp = this.timeGen();
      long offset = lastTimestamp - timestamp;
      if (offset < 0) {
        return timestamp;
      }
      if (offset >= 5) {
        // 系统时钟回调时间大于5ms
        throw new IllegalArgumentException(String
            .format("Clock moved backwards.Refusing to generate id for %d milliseconds",
                lastTimestamp - timestamp));
      }
      if (offset >= 2) {
        // 系统时钟回调时间大于等于2ms
        ThreadUtils.sleep(offset);
      }
    }
  }

  private long timeGen() {
    return System.currentTimeMillis();
  }

  @Data
  @Accessors(chain = true)
  public static class Cfg {

    /**
     * 起始时间
     */
    private long baseTime;
    /**
     * 工作机器id
     */
    private long workerId;
    /**
     * 数据中心id
     */
    private long dataCenterId;
    /**
     * 工作机器id存储位数
     */
    private long workerIdBits = -1;
    /**
     * 数据中心id存储位数
     */
    private long dataCenterIdBits = -1;
    /**
     * 序列号存储位数
     */
    private long sequenceBits = -1;
  }

  private static class InstanceHolder {

    static final SnowflakeId INSTANCE = new SnowflakeId(0,
        ThreadLocalRandom.current().nextLong(~(-1L << DATA_CENTER_ID_BITS)));

    private InstanceHolder() {
    }
  }

}
