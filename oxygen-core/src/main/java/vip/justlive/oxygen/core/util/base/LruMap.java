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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * lru map
 *
 * @author wubo
 */
public class LruMap<K, V> extends LinkedHashMap<K, V> {

  private static final long serialVersionUID = 871560280285034904L;

  private int maxSize;
  private transient BiConsumer<K, V> consumer;

  /**
   * 最大容量
   *
   * @param maxSize 容量
   * @return this
   */
  public LruMap<K, V> maxSize(int maxSize) {
    this.maxSize = maxSize;
    return this;
  }

  /**
   * 淘汰触发逻辑
   *
   * @param consumer 处理逻辑
   * @return this
   */
  public LruMap<K, V> evict(BiConsumer<K, V> consumer) {
    this.consumer = consumer;
    return this;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    boolean b = maxSize > 0 && maxSize < super.size();
    if (b && consumer != null) {
      consumer.accept(eldest.getKey(), eldest.getValue());
    }
    return b;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    LruMap<?, ?> lruMap = (LruMap<?, ?>) o;
    return maxSize == lruMap.maxSize &&
        Objects.equals(consumer, lruMap.consumer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), maxSize, consumer);
  }
}
