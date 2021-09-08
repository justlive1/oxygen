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
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.job.JobInfo;
import vip.justlive.oxygen.core.job.JobRunTask;
import vip.justlive.oxygen.core.job.JobStore;
import vip.justlive.oxygen.core.job.JobTrigger;
import vip.justlive.oxygen.core.job.Signaler;
import vip.justlive.oxygen.core.job.TriggerFiredResult;
import vip.justlive.oxygen.jdbc.Jdbc;
import vip.justlive.oxygen.jdbc.JdbcException;
import vip.justlive.oxygen.jdbc.handler.ResultSetHandler;
import vip.justlive.oxygen.jdbc.handler.StringResultHandler;
import vip.justlive.oxygen.jdbc.page.Page;
import vip.justlive.oxygen.jdbc.record.Entity;

/**
 * jdbc实现的job store
 *
 * @author wubo
 */
@Slf4j
public class JdbcJobStore implements JobStore {

  public static final String LOCK_TRIGGER_ACCESS = "trigger_access";

  static final int STATE_WAITING = 0;
  static final int STATE_ACQUIRED = 1;
  static final int STATE_COMPLETE = 2;
  static final int STATE_PAUSED = 3;

  private Signaler signaler;
  private String dataSourceName;

  private final TriggerConverter converter = new TriggerConverter();
  private final ThreadLocal<Set<String>> lockOwners = new ThreadLocal<>();

  @Override
  public void initialize(Signaler signaler) {
    this.signaler = signaler;
    this.dataSourceName = ConfigFactory
        .getProperty("oxygen.job.jdbc.dataSourceName", Jdbc.PRIMARY_KEY);
  }

  @Override
  public void storeJob(JobInfo jobInfo, boolean replaceExisting) {
    executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      JobInfoEntity entity = loadJob(conn, jobInfo.getKey());
      if (entity != null) {
        if (!replaceExisting) {
          throw new IllegalArgumentException("job '" + jobInfo.getKey() + "' already exists");
        }
        Entity.parse(JobInfoEntity.class).updateById(conn,
            new JobInfoEntity().setId(entity.getId()).setDescription(jobInfo.getDescription())
                .setParam(jobInfo.getParam()).setHandlerClass(jobInfo.getHandlerClass()));
      } else {
        Entity.parse(JobInfoEntity.class).insert(conn,
            new JobInfoEntity().setJobKey(jobInfo.getKey()).setDescription(jobInfo.getDescription())
                .setParam(jobInfo.getParam()).setHandlerClass(jobInfo.getHandlerClass()));
      }
      return null;
    });
  }

  @Override
  public JobInfo getJobInfo(String jobKey) {
    return executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      JobInfoEntity entity = loadJob(conn, jobKey);
      if (entity == null) {
        return null;
      }
      return new JobInfo().setKey(entity.getJobKey()).setDescription(entity.getDescription())
          .setHandlerClass(entity.getHandlerClass()).setParam(entity.getParam());
    });
  }

  @Override
  public void removeJob(String jobKey) {
    executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      JobInfoEntity entity = loadJob(conn, jobKey);
      if (entity == null) {
        return null;
      }
      Entity.parse(JobTriggerEntity.class)
          .delete(conn, new JobTriggerEntity().setJobKey(entity.getJobKey()));
      Entity.parse(JobInfoEntity.class).deleteById(conn, entity.getId());
      return null;
    });
  }

  @Override
  public void storeTrigger(JobTrigger trigger) {
    if (trigger == null) {
      return;
    }
    executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      JobTriggerEntity entity = loadTrigger(conn, trigger.getKey());
      if (entity != null) {
        throw new IllegalArgumentException("trigger key '" + trigger.getKey() + "' already exists");
      }

      JobInfoEntity jobEntity = loadJob(conn, trigger.getJobKey());
      if (jobEntity == null) {
        throw new IllegalArgumentException(
            "the job '" + trigger.getJobKey() + "' referenced by the trigger does not exist.");
      }
      Entity.parse(JobTriggerEntity.class)
          .insert(conn, converter.convert(trigger).setState(STATE_WAITING));
      return null;
    });
  }

  @Override
  public List<JobTrigger> getJobTrigger(String jobKey) {
    return executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      List<JobTriggerEntity> list = Entity.parse(JobTriggerEntity.class)
          .find(conn, new JobTriggerEntity().setJobKey(jobKey));
      List<JobTrigger> triggers = new ArrayList<>(list.size());
      for (JobTriggerEntity entity : list) {
        JobTrigger jobTrigger = converter.convert(entity);
        if (jobTrigger != null) {
          triggers.add(jobTrigger);
        }
      }
      return triggers;
    });
  }

  @Override
  public void removeTrigger(String triggerKey) {
    executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      Entity.parse(JobTriggerEntity.class)
          .delete(conn, new JobTriggerEntity().setTriggerKey(triggerKey));
      return null;
    });
  }

  @Override
  public void pauseJob(String jobKey) {
    executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      Entity<JobTriggerEntity> model = Entity.parse(JobTriggerEntity.class);
      List<JobTriggerEntity> triggers = model.find(conn, new JobTriggerEntity().setJobKey(jobKey));
      for (JobTriggerEntity trigger : triggers) {
        boolean shouldUpdate = trigger.getState() != null && (trigger.getState() == STATE_WAITING
            || trigger.getState() == STATE_ACQUIRED);
        if (shouldUpdate) {
          model.updateById(conn,
              new JobTriggerEntity().setId(trigger.getId()).setState(STATE_PAUSED));
        }
      }
      return null;
    });
  }

  @Override
  public void pauseTrigger(String triggerKey) {
    executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      Entity<JobTriggerEntity> model = Entity.parse(JobTriggerEntity.class);
      JobTriggerEntity trigger = model
          .findOne(conn, new JobTriggerEntity().setTriggerKey(triggerKey));
      boolean shouldUpdate =
          trigger != null && trigger.getState() != null && (trigger.getState() == STATE_WAITING
              || trigger.getState() == STATE_ACQUIRED);
      if (shouldUpdate) {
        model.updateById(conn,
            new JobTriggerEntity().setId(trigger.getId()).setState(STATE_PAUSED));
      }
      return null;
    });
  }

  @Override
  public void resumeJob(String jobKey) {
    executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      Entity<JobTriggerEntity> model = Entity.parse(JobTriggerEntity.class);
      List<JobTriggerEntity> triggers = model.find(conn, new JobTriggerEntity().setJobKey(jobKey));
      for (JobTriggerEntity trigger : triggers) {
        if (trigger.getState() != null && trigger.getState() == STATE_PAUSED) {
          model.updateById(conn,
              new JobTriggerEntity().setId(trigger.getId()).setState(STATE_WAITING));
        }
      }
      return null;
    });
  }

  @Override
  public void resumeTrigger(String triggerKey) {
    executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      Entity<JobTriggerEntity> model = Entity.parse(JobTriggerEntity.class);
      JobTriggerEntity trigger = model
          .findOne(conn, new JobTriggerEntity().setTriggerKey(triggerKey));
      if (trigger != null && trigger.getState() != null && trigger.getState() == STATE_PAUSED) {
        model.updateById(conn,
            new JobTriggerEntity().setId(trigger.getId()).setState(STATE_WAITING));
      }
      return null;
    });
  }

  @Override
  public List<JobTrigger> acquireNextTriggers(long maxTimestamp, int maxSize) {
    String lockName;
    if (maxSize > 1) {
      lockName = LOCK_TRIGGER_ACCESS;
    } else {
      lockName = null;
    }
    return executeInLock(lockName, conn -> acquireNextTriggers(conn, maxTimestamp, maxSize));
  }

  @Override
  public void releaseTrigger(JobTrigger trigger) {
    executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      Jdbc.update(conn, "update oxy_job_trigger set state = ? where trigger_key = ? and state = ?",
          STATE_ACQUIRED, trigger.getKey(), STATE_WAITING);
      return null;
    });
  }

  @Override
  public TriggerFiredResult triggerFired(JobTrigger trigger) {
    return executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      Entity<JobInfoEntity> model = Entity.parse(JobInfoEntity.class);
      JobInfoEntity entity = model
          .findOne(conn, new JobInfoEntity().setJobKey(trigger.getJobKey()));
      if (entity == null) {
        return null;
      }
      Entity<JobTriggerEntity> triggerModel = Entity.parse(JobTriggerEntity.class);
      JobTriggerEntity triggerEntity = triggerModel
          .findOne(conn, new JobTriggerEntity().setTriggerKey(trigger.getKey()));
      if (triggerEntity == null || triggerEntity.getState() == null
          || triggerEntity.getState() != STATE_ACQUIRED) {
        return null;
      }

      JobTrigger newTrigger = converter.convert(triggerEntity);
      newTrigger.computeNextFireTime(System.currentTimeMillis());

      JobTriggerEntity newTriggerEntity = converter.convert(newTrigger);
      if (newTrigger.getNextFireTime() == null) {
        newTriggerEntity.setState(STATE_COMPLETE);
      } else {
        newTriggerEntity.setState(STATE_WAITING);
      }
      newTriggerEntity.setId(triggerEntity.getId());
      triggerModel.updateById(conn, newTriggerEntity);

      return new TriggerFiredResult(newTrigger, null);
    });
  }

  @Override
  public void triggerCompleted(JobTrigger trigger, int state) {
    executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      if (state == JobRunTask.DELETE) {
        removeTrigger(trigger.getKey());
      }

      JobTriggerEntity triggerEntity = loadTrigger(conn, trigger.getKey());
      if (triggerEntity == null) {
        return null;
      }

      JobTrigger newTrigger = converter.convert(triggerEntity);
      long lastCompletedTime = System.currentTimeMillis();

      newTrigger.setLastCompletedTime(lastCompletedTime);
      trigger.setLastCompletedTime(lastCompletedTime);

      Entity.parse(JobTriggerEntity.class)
          .updateById(converter.convert(newTrigger).setId(triggerEntity.getId()));
      return null;
    });
    signaler.schedulingChange();
  }

  private List<JobTrigger> acquireNextTriggers(Connection conn, long maxTimestamp, int maxSize) {
    List<JobTrigger> triggers = new ArrayList<>();

    long noEarlierThan = System.currentTimeMillis();
    List<JobTriggerEntity> list = Jdbc.query(conn,
        "select * from oxy_job_trigger where next_fire_time <= ? and next_fire_time >= ? and state = ? order by next_fire_time",
        ResultSetHandler.beanListHandler(JobTriggerEntity.class), maxTimestamp, noEarlierThan,
        STATE_WAITING, new Page<>(1, maxSize));

    for (JobTriggerEntity entity : list) {
      Long nextFireTime = entity.getNextFireTime();
      if (nextFireTime != null) {
        if (nextFireTime > maxTimestamp) {
          break;
        }
        int updated = Jdbc.update(conn,
            "update oxy_job_trigger set state = ? where trigger_key = ? and state = ?",
            STATE_ACQUIRED, entity.getTriggerKey(), STATE_WAITING);
        if (updated > 0) {
          triggers.add(converter.convert(entity));
        }
      } else {
        log.warn("the nextFireTime of Trigger {} is null", entity.getTriggerKey());
      }
    }
    return triggers;
  }

  private JobInfoEntity loadJob(Connection conn, String jobKey) {
    Entity<JobInfoEntity> model = Entity.parse(JobInfoEntity.class);
    return model.findOne(conn, new JobInfoEntity().setJobKey(jobKey));
  }

  private JobTriggerEntity loadTrigger(Connection conn, String triggerKey) {
    Entity<JobTriggerEntity> model = Entity.parse(JobTriggerEntity.class);
    return model.findOne(conn, new JobTriggerEntity().setTriggerKey(triggerKey));
  }


  private Set<String> getThreadLocks() {
    Set<String> threadLocks = lockOwners.get();
    if (threadLocks == null) {
      threadLocks = new HashSet<>();
      lockOwners.set(threadLocks);
    }
    return threadLocks;
  }

  protected <T> T executeInLock(String lockName, Function<Connection, T> function) {
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

  protected boolean obtainLock(Connection conn, String lockName) {
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

  protected void releaseLock(String lockName, boolean isLocked) {
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
        lockOwners.remove();
      }
    } else if (log.isDebugEnabled()) {
      log.warn("Lock '{}' attempt to return by: {}-{} -- but not owner!", lockName,
          Thread.currentThread().getName(), Thread.currentThread().getId());
    }
  }

}
