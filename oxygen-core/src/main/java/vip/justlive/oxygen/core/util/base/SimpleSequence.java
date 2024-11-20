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

import java.util.concurrent.atomic.AtomicLong;

/**
 * 简单序列
 *
 * @author wubo
 */
public class SimpleSequence implements Sequence<Long> {
  
  private final AtomicLong sequence;
  
  public SimpleSequence() {
    this(0);
  }
  
  public SimpleSequence(long initialValue) {
    this.sequence = new AtomicLong(initialValue);
  }
  
  @Override
  public Long nextId() {
    return sequence.getAndIncrement();
  }
}
