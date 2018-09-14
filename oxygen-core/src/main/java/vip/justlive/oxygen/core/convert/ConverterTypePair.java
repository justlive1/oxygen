package vip.justlive.oxygen.core.convert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import vip.justlive.oxygen.core.util.Checks;

/**
 * 转换类型对
 *
 * @author wubo
 */
public class ConverterTypePair {

  static final Map<Integer, ConverterTypePair> CACHE = new ConcurrentHashMap<>(8);

  /**
   * 源类型
   */
  Class<?> sourceType;
  /**
   * 目标类型
   */
  Class<?> targetType;

  private ConverterTypePair(Class<?> sourceType, Class<?> targetType) {
    this.sourceType = sourceType;
    this.targetType = targetType;
  }

  public static ConverterTypePair create(Class<?> sourceType, Class<?> targetType) {
    Checks.notNull(sourceType, "Source type must not be null");
    Checks.notNull(targetType, "Target type must not be null");
    Integer key = hash(sourceType, targetType);
    ConverterTypePair pair = CACHE.get(key);
    if (pair == null) {
      pair = new ConverterTypePair(sourceType, targetType);
      ConverterTypePair out = CACHE.putIfAbsent(key, pair);
      if (out != null) {
        return out;
      }
    }
    return pair;
  }

  private static int hash(Class<?> sourceType, Class<?> targetType) {
    return (sourceType.hashCode() * 31 + targetType.hashCode());
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || other.getClass() != ConverterTypePair.class) {
      return false;
    }
    ConverterTypePair otherPair = (ConverterTypePair) other;
    return (this.sourceType == otherPair.sourceType && this.targetType == otherPair.targetType);
  }

  @Override
  public int hashCode() {
    return hash(this.sourceType, this.targetType);
  }

  @Override
  public String toString() {
    return (this.sourceType.getName() + " -> " + this.targetType.getName());
  }

}
