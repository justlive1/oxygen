/*
 * Copyright (C) 2022 the original author or authors.
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

import java.util.prefs.Preferences;
import lombok.Getter;

/**
 * 单实例序列，重启可以继续上次序列
 *
 * @author wubo
 */
public class SingletonSequence implements Sequence<Long> {
  
  private static final String KEY = "sequence";
  private static final Preferences PF = Preferences.userNodeForPackage(SingletonSequence.class);
  
  private long sequence;
  private long maxId = 0;
  
  @Getter
  private final long cacheSize;
  
  public SingletonSequence() {
    this(1);
  }
  
  public SingletonSequence(int cacheSize) {
    this.cacheSize = cacheSize;
    long value = PF.getLong(KEY, maxId);
    sequence = value;
    value += getCacheSize();
    PF.putLong(KEY, value);
    maxId = value;
  }
  
  @Override
  public synchronized Long nextId() {
    if (sequence == maxId) {
      long value = PF.getLong(KEY, maxId);
      value += getCacheSize();
      PF.putLong(KEY, value);
      maxId = value;
      
      sequence = maxId - getCacheSize() + 1;
    } else {
      sequence++;
    }
    return sequence;
  }
  
  public void setInitialSequence(long initialValue) {
    sequence = initialValue;
    maxId = sequence + getCacheSize();
    PF.putLong(KEY, maxId);
  }
  
}
