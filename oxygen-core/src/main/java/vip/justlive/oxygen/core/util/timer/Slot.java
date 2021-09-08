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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;

/**
 * 插槽
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class Slot implements Delayed {

  private final AtomicLong deadline = new AtomicLong(-1);

  Task<?> head;
  Task<?> tail;

  static Slot[] createSlots(int ticks) {
    if (ticks <= 0) {
      throw new IllegalArgumentException("ticks must be greater than 0: " + ticks);
    }
    Slot[] sts = new Slot[ticks];
    for (int i = 0; i < sts.length; i++) {
      sts[i] = new Slot();
    }
    return sts;
  }

  synchronized void addTask(Task<?> task) {
    if (head == null) {
      head = tail = task;
    } else {
      tail.next = task;
      task.prev = tail;
      tail = task;
    }
    task.slot = this;
  }

  synchronized Task<?> remove(Task<?> task) {
    Task<?> next = task.next;
    if (task.prev != null) {
      task.prev.next = next;
    }
    if (task.next != null) {
      task.next.prev = task.prev;
    }
    if (task == head) {
      if (task == tail) {
        tail = null;
        head = null;
      } else {
        head = next;
      }
    } else if (task == tail) {
      tail = task.prev;
    }
    task.prev = null;
    task.next = null;
    task.slot = null;
    return next;
  }

  boolean setDeadline(long deadline) {
    return this.deadline.getAndSet(deadline) != deadline;
  }

  long getDeadline() {
    return deadline.get();
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(deadline.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(Delayed o) {
    Slot slot = (Slot) o;
    return Long.compare(deadline.get(), slot.deadline.get());
  }
}
