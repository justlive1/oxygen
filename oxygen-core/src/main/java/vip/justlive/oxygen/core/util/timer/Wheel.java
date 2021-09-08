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

import java.util.concurrent.DelayQueue;

/**
 * 轮盘
 *
 * @author wubo
 */
public class Wheel {

  private final long duration;
  private final long interval;
  private final Slot[] slots;
  private final DelayQueue<Slot> delayQueue;

  private long currentTime;
  private Wheel overflowWheel;

  public Wheel(long duration, int wheelSize, long startTime, DelayQueue<Slot> delayQueue) {
    this.duration = duration;
    this.interval = duration * wheelSize;
    this.slots = Slot.createSlots(wheelSize);
    // 取整
    this.currentTime = startTime - (startTime % duration);
    this.delayQueue = delayQueue;
  }

  boolean add(Task<?> task) {
    if (task.isCancelled()) {
      return false;
    }
    if (task.deadline < currentTime + duration) {
      // 已过期
      return false;
    }
    if (task.deadline < currentTime + interval) {
      // 在当前时间轮周期内
      long virtualId = task.deadline / duration;
      Slot slot = slots[(int) (virtualId % slots.length)];
      slot.addTask(task);
      // 只会添加到队列中一次
      if (slot.setDeadline(virtualId * duration)) {
        // always true
        delayQueue.offer(slot);
      }
      return true;
    }
    if (overflowWheel == null) {
      addOverflowWheel();
    }
    return overflowWheel.add(task);
  }

  void advanceClock(long currentTime) {
    if (currentTime >= this.currentTime + duration) {
      this.currentTime = currentTime - (currentTime % duration);
    }
    if (overflowWheel != null) {
      overflowWheel.advanceClock(this.currentTime);
    }
  }

  private synchronized void addOverflowWheel() {
    if (overflowWheel == null) {
      overflowWheel = new Wheel(interval, slots.length, currentTime, delayQueue);
    }
  }
}
