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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * 有失效时间的 Map，可对每个键值对设置失效时间
 * <p>
 * Example usages:
 *
 * <pre>
 * {@code
 *   Map<String, Integer> map = ExpiringMap.<String, Integer>builder()
 *       .expiration(30, TimeUnit.SECONDS).expiringPolicy(ExpiringPolicy.ACCESSED)
 *       .cleanPolicy(CleanPolicy.ACCUMULATE).accumulateThreshold(50).build();
 *
 *   Map<String, Integer> map = ExpiringMap.<String, Integer>builder()
 *       .expiration(30, TimeUnit.SECONDS).expiringPolicy(ExpiringPolicy.CREATED)
 *       .cleanPolicy(CleanPolicy.SCHEDULE).scheduleDelay(60).build();
 * }
 * </pre>
 *
 * @param <K> 泛型类
 * @param <V> 泛型类
 * @author wubo
 */
@Slf4j
public class ExpiringMap<K, V> implements ConcurrentMap<K, V>, Serializable {

  private static final long serialVersionUID = 1L;
  private static final AtomicInteger INS = new AtomicInteger(0);

  private final transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final transient Lock readLock = readWriteLock.readLock();
  private final transient Lock writeLock = readWriteLock.writeLock();
  private final transient AtomicLong accumulate = new AtomicLong();
  /**
   * 失效监听
   */
  private final transient List<ExpiredListener<K, V>> asyncExpiredListeners;
  /**
   * 名称
   */
  @Getter
  private final String name;
  /**
   * 最大数量
   */
  @Getter
  private final int maxSize;
  /**
   * 有效期
   */
  @Getter
  private final long duration;
  /**
   * 有效期单位
   */
  @Getter
  private final TimeUnit timeUnit;
  /**
   * 失效策略
   */
  @Getter
  private final ExpiringPolicy expiringPolicy;
  /**
   * 失效清除策略
   */
  @Getter
  private final CleanPolicy cleanPolicy;
  /**
   * 定时清理延迟
   */
  @Getter
  private final int scheduleDelay;
  /**
   * 累积阈值
   */
  @Getter
  private final int accumulateThreshold;
  /**
   * 实际数据
   */
  private final LruMap<K, ExpiringValue<V>> data;

  /**
   * 构造函数
   *
   * @param builder 构造器
   */
  private ExpiringMap(final Builder<K, V> builder) {
    asyncExpiredListeners = builder.asyncExpiredListeners;
    maxSize = builder.maxSize;
    duration = builder.duration;
    timeUnit = builder.timeUnit;
    cleanPolicy = builder.cleanPolicy;
    expiringPolicy = builder.expiringPolicy;
    if (builder.scheduleDelay <= 0) {
      scheduleDelay = Math.max((int) timeUnit.toSeconds(duration) / 4, 60);
    } else {
      scheduleDelay = builder.scheduleDelay;
    }
    accumulateThreshold = builder.accumulateThreshold;
    int index = INS.getAndIncrement();
    name = MoreObjects.firstNonNull(builder.name, String.format("Unnamed-%d", index));
    ThreadUtils.globalTimer()
        .scheduleWithFixedDelay(this::runScheduleCleanPolicy, scheduleDelay, scheduleDelay,
            TimeUnit.SECONDS);
    data = new LruMap<K, ExpiringValue<V>>().maxSize(maxSize);
    if (asyncExpiredListeners != null) {
      data.evict(this::evict);
    }
  }

  /**
   * 获取构造器
   *
   * @param <K> 泛型类
   * @param <V> 泛型类
   * @return 构造器
   */
  public static <K, V> Builder<K, V> builder() {
    return new Builder<>();
  }

  /**
   * 获取默认ExpiringMap，最大值Integer.MAX_VALUE
   *
   * @param <K> 泛型类
   * @param <V> 泛型类
   * @return ExpiringMap
   */
  public static <K, V> ExpiringMap<K, V> create() {
    return ExpiringMap.<K, V>builder().build();
  }

  /**
   * 真实数量
   *
   * @return 数量
   */
  public int realSize() {
    readLock.lock();
    try {
      return data.size();
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public int size() {
    readLock.lock();
    try {
      return (int) data.values().parallelStream().filter(r -> !r.isExpired()).count();
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public boolean isEmpty() {
    return this.size() > 0;
  }

  @Override
  public boolean containsKey(Object key) {
    readLock.lock();
    try {
      ExpiringValue<V> value = data.get(key);
      return value != null && !value.isExpired();
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public boolean containsValue(Object value) {
    readLock.lock();
    try {
      for (Entry<K, ExpiringValue<V>> entry : data.entrySet()) {
        ExpiringValue<V> wrapValue = entry.getValue();
        if (!wrapValue.isExpired() && Objects.equals(wrapValue.value, value)) {
          return true;
        }
      }
      return false;
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public V get(Object key) {
    readLock.lock();
    try {
      ExpiringValue<V> value = data.get(key);
      if (value == null || value.isExpired()) {
        return null;
      }
      accessRecord(value);
      return value.value;
    } finally {
      readLock.unlock();
      record();
    }
  }

  @Override
  public V put(K key, V value) {
    return put(key, value, duration);
  }

  /**
   * 添加默认时间单位的会失效的键值
   *
   * @param key      键
   * @param value    值
   * @param duration 期限
   * @return 已存在的值
   */
  public V put(K key, V value, long duration) {
    return put(key, value, duration, this.timeUnit);
  }

  /**
   * 添加会失效的键值
   *
   * @param key      键
   * @param value    值
   * @param duration 期限
   * @param timeUnit 时间单位
   * @return 已存在的值
   */
  public V put(K key, V value, long duration, TimeUnit timeUnit) {
    writeLock.lock();
    try {
      ExpiringValue<V> wrapValue = new ExpiringValue<>(value, duration, timeUnit);
      ExpiringValue<V> preValue = data.put(key, wrapValue);
      if (preValue != null) {
        if (!Objects.equals(preValue.value, value)) {
          notifyListener(key, preValue.value, RemovalCause.REPLACED);
        }
        if (!preValue.isExpired()) {
          return preValue.value;
        }
      }
      return null;
    } finally {
      writeLock.unlock();
      record();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public V remove(Object key) {
    writeLock.lock();
    try {
      ExpiringValue<V> value = data.remove(key);
      if (value == null || value.isExpired()) {
        return null;
      }
      notifyListener((K) key, value.value, RemovalCause.EXPLICIT);
      return value.value;
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    writeLock.lock();
    try {
      for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
        ExpiringValue<V> preValue = data
            .put(entry.getKey(), new ExpiringValue<>(entry.getValue(), duration, timeUnit));
        if (preValue != null && !Objects.equals(preValue.value, entry.getValue())) {
          notifyListener(entry.getKey(), preValue.value, RemovalCause.REPLACED);
        }
      }
    } finally {
      writeLock.unlock();
      record();
    }
  }

  @Override
  public void clear() {
    writeLock.lock();
    try {
      Iterator<Map.Entry<K, ExpiringValue<V>>> it = data.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<K, ExpiringValue<V>> item = it.next();
        it.remove();
        notifyListener(item.getKey(), item.getValue().value, RemovalCause.EXPLICIT);
      }
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public Set<K> keySet() {
    readLock.lock();
    try {
      return data.entrySet().parallelStream().filter(r -> !r.getValue().isExpired())
          .map(Entry::getKey).collect(Collectors.toSet());
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public Collection<V> values() {
    readLock.lock();
    try {
      return data.values().parallelStream().filter(r -> !r.isExpired()).map(r -> r.value)
          .collect(Collectors.toList());
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    readLock.lock();
    try {
      return data.entrySet().parallelStream().filter(r -> !r.getValue().isExpired())
          .map(r -> new MapEntry<>(r.getKey(), r.getValue().value)).collect(Collectors.toSet());
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public V putIfAbsent(K key, V value) {
    if (duration > 0) {
      return putIfAbsent(key, value, duration);
    }
    return putIfAbsent(key, new ExpiringValue<>(value));
  }

  public V putIfAbsent(K key, V value, long duration) {
    return putIfAbsent(key, value, duration, timeUnit);
  }

  public V putIfAbsent(K key, V value, long duration, TimeUnit timeUnit) {
    return putIfAbsent(key, new ExpiringValue<>(value, duration, timeUnit));
  }

  @Override
  public boolean remove(Object key, Object value) {
    writeLock.lock();
    try {
      @SuppressWarnings("unchecked") K k = (K) key;
      ExpiringValue<V> wrapValue = data.get(k);
      if (wrapValue != null && !wrapValue.isExpired() && Objects.equals(wrapValue.value, value)) {
        notifyListener(k, wrapValue.value, RemovalCause.EXPLICIT);
        data.remove(k);
        return true;
      }
      return false;
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    writeLock.lock();
    try {
      ExpiringValue<V> wrapValue = data.get(key);
      if (wrapValue != null && !wrapValue.isExpired() && Objects
          .equals(wrapValue.value, oldValue)) {
        ExpiringValue<V> newWrapValue = new ExpiringValue<>(newValue);
        newWrapValue.duration = wrapValue.duration;
        newWrapValue.expireAt = wrapValue.expireAt;
        if (!Objects.equals(newValue, wrapValue.value)) {
          notifyListener(key, wrapValue.value, RemovalCause.REPLACED);
        }
        data.put(key, newWrapValue);
        return true;
      }
      return false;
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public V replace(K key, V value) {
    writeLock.lock();
    try {
      ExpiringValue<V> wrapValue = data.get(key);
      if (wrapValue != null && !wrapValue.isExpired()) {
        ExpiringValue<V> newWrapValue = new ExpiringValue<>(value);
        newWrapValue.duration = wrapValue.duration;
        newWrapValue.expireAt = wrapValue.expireAt;
        wrapValue = data.put(key, newWrapValue);
        if (wrapValue != null) {
          if (!Objects.equals(wrapValue.value, value)) {
            notifyListener(key, wrapValue.value, RemovalCause.REPLACED);
          }
          return wrapValue.value;
        }
      }
      return null;
    } finally {
      writeLock.unlock();
    }
  }

  public V replace(K key, V value, long duration, TimeUnit timeUnit) {
    writeLock.lock();
    try {
      ExpiringValue<V> wrapValue = data.get(key);
      if (wrapValue != null && !wrapValue.isExpired()) {
        ExpiringValue<V> newWrapValue = new ExpiringValue<>(value, duration, timeUnit);
        wrapValue = data.put(key, newWrapValue);
        if (wrapValue != null) {
          if (!Objects.equals(wrapValue.value, value)) {
            notifyListener(key, wrapValue.value, RemovalCause.REPLACED);
          }
          return wrapValue.value;
        }
      }
      return null;
    } finally {
      writeLock.unlock();
    }
  }

  private V putIfAbsent(K key, ExpiringValue<V> wrapValue) {
    writeLock.lock();
    try {
      ExpiringValue<V> preVal = data.putIfAbsent(key, wrapValue);
      if (preVal == null) {
        return null;
      }
      if (preVal.expireAt < System.currentTimeMillis()) {
        data.put(key, wrapValue);
        if (!Objects.equals(wrapValue.value, preVal.value)) {
          notifyListener(key, preVal.value, RemovalCause.EXPIRED);
        }
        return null;
      }
      return preVal.value;
    } finally {
      writeLock.unlock();
      record();
    }
  }


  private void record() {
    accumulate.getAndIncrement();
    checkAccumulate();
  }

  private void accessRecord(ExpiringValue<V> value) {
    if (expiringPolicy == ExpiringPolicy.ACCESSED) {
      value.expireAt = System.currentTimeMillis() + value.duration;
    }
  }

  private void checkAccumulate() {
    if (cleanPolicy == CleanPolicy.ACCUMULATE && accumulate.get() % accumulateThreshold == 0) {
      runAccumulateCleanPolicy();
    }
  }

  private void expiredClean() {
    writeLock.lock();
    try {
      Iterator<Entry<K, ExpiringValue<V>>> it = data.entrySet().iterator();
      while (it.hasNext()) {
        Entry<K, ExpiringValue<V>> entry = it.next();
        if (entry.getValue().isExpired()) {
          K key = entry.getKey();
          V value = entry.getValue().value;
          it.remove();
          notifyListenerSync(key, value, RemovalCause.EXPIRED);
        }
      }
    } finally {
      writeLock.unlock();
    }
  }

  private void notifyListenerSync(K key, V value, RemovalCause cause) {
    if (asyncExpiredListeners != null && !asyncExpiredListeners.isEmpty()) {
      for (ExpiredListener<K, V> listener : asyncExpiredListeners) {
        listener.expire(key, value, cause);
      }
    }
  }

  private void notifyListener(K key, V value, RemovalCause cause) {
    ThreadUtils.globalPool().execute(() -> this.notifyListenerSync(key, value, cause));
  }

  private void runScheduleCleanPolicy() {
    int size = this.realSize();
    if (log.isDebugEnabled() && size > 0) {
      log.debug("scheduled clean [{}] and now realSize is [{}]", this.name, size);
    }
    this.expiredClean();
  }

  private void runAccumulateCleanPolicy() {
    int size = this.realSize();
    if (log.isDebugEnabled() && size > 0) {
      log.debug("accumulate clean [{}] and now realSize is [{}]", this.name, size);
    }
    ThreadUtils.globalPool().execute(this::expiredClean);
  }

  private void evict(K key, ExpiringValue<V> value) {
    for (ExpiredListener<K, V> listener : asyncExpiredListeners) {
      listener.expire(key, value.value, RemovalCause.SIZE);
    }
  }

  /**
   * 失效策略
   *
   * @author wubo
   */
  public enum ExpiringPolicy {
    /**
     * 创建后duration失效
     */
    CREATED,
    /**
     * 访问键值刷新duration
     */
    ACCESSED
  }

  /**
   * 失效清除策略
   *
   * @author wubo
   */
  public enum CleanPolicy {
    /**
     * 定时任务
     */
    SCHEDULE,
    /**
     * 累计
     */
    ACCUMULATE
  }

  /**
   * 失效原因
   */
  public enum RemovalCause {
    /**
     * 用户主动删除 {@link #remove(Object)},{@link #remove(Object, Object)},{@link #clear()}
     */
    EXPLICIT,
    /**
     * 用户替换值 {@link #replace(Object, Object)}, {@link #replace(Object, Object, Object)}, {@link
     * #replace(Object, Object, long, TimeUnit)}, {@link #put(Object, Object)}, {@link #put(Object,
     * Object, long)}, {@link #put(Object, Object, long, TimeUnit)}
     */
    REPLACED,
    /**
     * 缓存正常失效
     */
    EXPIRED,
    /**
     * 由于容量失效 {@link Builder#maxSize(int)}
     */
    SIZE
  }

  /**
   * 失效监听
   *
   * @param <K> 泛型
   * @param <V> 泛型
   * @author wubo
   */
  @FunctionalInterface
  public interface ExpiredListener<K, V> {

    /**
     * 失效处理
     *
     * @param key   建
     * @param value 值
     * @param cause 原因
     */
    void expire(K key, V value, RemovalCause cause);

  }

  /**
   * 构建器
   *
   * @param <K> 泛型
   * @param <V> 泛型
   * @author wubo
   */
  public static final class Builder<K, V> {

    private List<ExpiredListener<K, V>> asyncExpiredListeners;
    private int maxSize = Integer.MAX_VALUE;
    private long duration = -1;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private ExpiringPolicy expiringPolicy = ExpiringPolicy.CREATED;
    private CleanPolicy cleanPolicy = CleanPolicy.ACCUMULATE;
    private int scheduleDelay = -1;
    private int accumulateThreshold = 10000;
    private String name;

    private Builder() {
    }

    /**
     * 设置名称
     *
     * @param name 名称
     * @return builder
     */
    public Builder<K, V> name(String name) {
      this.name = name;
      return this;
    }

    /**
     * 设置最大数量
     *
     * @param maxSize 最大数量
     * @return 构造器
     */
    public Builder<K, V> maxSize(int maxSize) {
      if (maxSize <= 0) {
        throw new IllegalArgumentException("[maxSize] should be positive");
      }
      this.maxSize = maxSize;
      return this;
    }

    /**
     * 增加单个异步失效监听
     *
     * @param listener 监听
     * @return 构造器
     */
    public Builder<K, V> asyncExpiredListeners(ExpiredListener<K, V> listener) {
      MoreObjects.notNull(listener, "[listener] can not be null");
      if (asyncExpiredListeners == null) {
        asyncExpiredListeners = new ArrayList<>();
      }
      asyncExpiredListeners.add(listener);
      return this;
    }

    /**
     * 增加异步失效监听列表
     *
     * @param listeners 监听
     * @return 构造器
     */
    public Builder<K, V> asyncExpiredListeners(List<ExpiredListener<K, V>> listeners) {
      MoreObjects.notNull(listeners, "[listeners] can not be null");
      asyncExpiredListeners = listeners;
      return this;
    }

    /**
     * 默认有效期
     *
     * @param duration 有效期限
     * @param timeUnit 时间单位
     * @return 构造器
     */
    public Builder<K, V> expiration(long duration, TimeUnit timeUnit) {
      if (duration <= 0) {
        throw new IllegalArgumentException("[duration] should be positive");
      }
      MoreObjects.notNull(timeUnit, "[timeUnit] can not be null");
      this.duration = duration;
      this.timeUnit = timeUnit;
      return this;
    }

    /**
     * 设置失效策略
     *
     * @param expiringPolicy 失效策略
     * @return 构造器
     */
    public Builder<K, V> expiringPolicy(ExpiringPolicy expiringPolicy) {
      MoreObjects.notNull(expiringPolicy, "[expiringPolicy] can not be null");
      this.expiringPolicy = expiringPolicy;
      return this;
    }

    /**
     * 设置失效清理策略
     *
     * @param cleanPolicy 失效清理策略
     * @return 构造器
     */
    public Builder<K, V> cleanPolicy(CleanPolicy cleanPolicy) {
      MoreObjects.notNull(cleanPolicy, "[cleanPolicy] can not be null");
      this.cleanPolicy = cleanPolicy;
      return this;
    }

    /**
     * 设置定时清理的延迟
     *
     * @param scheduleDelay 延迟
     * @return 构造器
     */
    public Builder<K, V> scheduleDelay(int scheduleDelay) {
      if (scheduleDelay <= 0) {
        throw new IllegalArgumentException("[scheduleDelay] should be positive");
      }
      this.scheduleDelay = scheduleDelay;
      return this;
    }

    /**
     * 设置累积阈值
     *
     * @param accumulateThreshold 阈值
     * @return 构造器
     */
    public Builder<K, V> accumulateThreshold(int accumulateThreshold) {
      if (accumulateThreshold <= 0) {
        throw new IllegalArgumentException("[accumulateThreshold] should be positive");
      }
      this.accumulateThreshold = accumulateThreshold;
      return this;
    }

    /**
     * 构造ExpiringMap
     *
     * @return ExpiringMap
     */
    public ExpiringMap<K, V> build() {
      return new ExpiringMap<>(this);
    }
  }

  /**
   * 带失效时间的包装
   *
   * @param <V> 泛型
   */
  private static final class ExpiringValue<V> {

    /**
     * 不过期
     */
    private static final long NOT_EXPIRED = -1L;

    /**
     * 值
     */
    private final V value;

    /**
     * 过期时间
     */
    private long expireAt;

    /**
     * 期限
     */
    private long duration;

    /**
     * 构造不过期的包装
     *
     * @param value 值
     */
    ExpiringValue(V value) {
      this.value = value;
      this.expireAt = NOT_EXPIRED;
    }

    /**
     * 构造带过期时间的包装
     *
     * @param value    值
     * @param duration 期限
     * @param timeUnit 时间单位
     */
    ExpiringValue(V value, long duration, TimeUnit timeUnit) {
      this.value = value;
      if (duration > 0) {
        this.duration = timeUnit.toMillis(duration);
        this.expireAt = System.currentTimeMillis() + this.duration;
      } else {
        this.expireAt = NOT_EXPIRED;
      }
    }

    /**
     * 是否过期
     *
     * @return true是过期
     */
    boolean isExpired() {
      return expireAt != NOT_EXPIRED && expireAt < System.currentTimeMillis();
    }

  }

  static final class MapEntry<K, V> implements Entry<K, V> {

    K key;
    V value;

    MapEntry(K key, V value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return value;
    }

    @Override
    public V setValue(V value) {
      V pre = this.value;
      this.value = value;
      return pre;
    }
  }
}
