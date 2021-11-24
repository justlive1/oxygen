/*
 * Copyright (C) 2021 the original author or authors.
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
package vip.justlive.oxygen.jdbc.job;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.bean.Singleton;
import vip.justlive.oxygen.core.job.CoreJobTrigger;
import vip.justlive.oxygen.core.job.JobTrigger;
import vip.justlive.oxygen.jdbc.Jdbc;
import vip.justlive.oxygen.jdbc.JdbcException;
import vip.justlive.oxygen.jdbc.handler.StringResultHandler;

/**
 * 工具集
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class Utils {
  
  public final String LOCK_TRIGGER_ACCESS = "trigger_access";
  
  private final List<Converter> CONVERTERS = new ArrayList<>();
  private final ThreadLocal<Set<String>> LOCK_OWNERS = new ThreadLocal<>();
  
  static {
    CONVERTERS.add(new FixedTimeJobTriggerConverter());
    CONVERTERS.add(new DelayOrRateJobTriggerConverter());
    CONVERTERS.add(new CronJobTriggerConverter());
    CONVERTERS.addAll(Singleton.getList(Converter.class));
  }
  
  public JobTriggerEntity convert(JobTrigger trigger) {
    for (Converter converter : CONVERTERS) {
      if (converter.classType() == trigger.getClass()) {
        return converter.convert(trigger);
      }
    }
    return null;
  }
  
  public JobTrigger convert(JobTriggerEntity entity) {
    if (entity.getTriggerType() == null) {
      return null;
    }
    for (Converter converter : CONVERTERS) {
      if (converter.type() == entity.getTriggerType()) {
        return converter.convert(entity);
      }
    }
    return null;
  }
  
  public Set<String> getThreadLocks() {
    Set<String> threadLocks = LOCK_OWNERS.get();
    if (threadLocks == null) {
      threadLocks = new HashSet<>();
      LOCK_OWNERS.set(threadLocks);
    }
    return threadLocks;
  }
  
  public <T> T executeInLock(String dataSourceName, String lockName,
      Function<Connection, T> function) {
    boolean isLocked = false;
    Connection conn = null;
    try {
      conn = Jdbc.getConnection(dataSourceName);
      try {
        conn.setAutoCommit(false);
      } catch (SQLException e) {
        throw JdbcException.wrap(e);
      }
      if (lockName != null) {
        isLocked = obtainLock(conn, lockName);
      }
      return function.apply(conn);
    } finally {
      try {
        releaseLock(lockName, isLocked);
      } finally {
        Jdbc.closeTx(conn);
      }
    }
  }
  
  public boolean obtainLock(Connection conn, String lockName) {
    if (!getThreadLocks().contains(lockName)) {
      String name = Jdbc.query(conn, "select name from oxy_lock where name = ? for update",
          new StringResultHandler(), lockName);
      if (log.isDebugEnabled()) {
        log.debug("Lock '{}' given to: {}-{}", name, Thread.currentThread().getName(),
            Thread.currentThread().getId());
      }
      getThreadLocks().add(lockName);
    } else if (log.isDebugEnabled()) {
      log.debug("Lock '{}' Is already owned by: {}-{}", lockName, Thread.currentThread().getName(),
          Thread.currentThread().getId());
    }
    return true;
  }
  
  public void releaseLock(String lockName, boolean isLocked) {
    if (!isLocked) {
      return;
    }
    Set<String> locks = getThreadLocks();
    if (locks.contains(lockName)) {
      if (log.isDebugEnabled()) {
        log.debug("Lock '{}' returned by: {}-{}", lockName, Thread.currentThread().getName(),
            Thread.currentThread().getId());
      }
      locks.remove(lockName);
      if (locks.isEmpty()) {
        LOCK_OWNERS.remove();
      }
    } else if (log.isDebugEnabled()) {
      log.warn("Lock '{}' attempt to return by: {}-{} -- but not owner!", lockName,
          Thread.currentThread().getName(), Thread.currentThread().getId());
    }
  }
  
  public void fillTriggerProperty(CoreJobTrigger trigger, JobTriggerEntity entity) {
    trigger.setStartTime(entity.getStartTime());
    trigger.setEndTime(entity.getEndTime());
    trigger.setPreviousFireTime(entity.getPreviousFireTime());
    trigger.setNextFireTime(entity.getNextFireTime());
    trigger.setLastCompletedTime(entity.getLastCompletedTime());
    trigger.setRounds(new AtomicLong(entity.getRounds()));
    trigger.setState(entity.getState());
  }
  
  public void fillEntityProperty(JobTriggerEntity entity, CoreJobTrigger jobTrigger) {
    entity.setJobKey(jobTrigger.getJobKey())
        .setTriggerKey(jobTrigger.getKey())
        .setStartTime(jobTrigger.getEndTime())
        .setEndTime(jobTrigger.getEndTime())
        .setPreviousFireTime(jobTrigger.getPreviousFireTime())
        .setNextFireTime(jobTrigger.getNextFireTime())
        .setLastCompletedTime(jobTrigger.getLastCompletedTime())
        .setRounds(jobTrigger.getRounds().get());
  }
}
