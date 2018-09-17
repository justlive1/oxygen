package vip.justlive.oxygen.core;

/**
 * 插件
 *
 * @author wubo
 */
public interface Plugin extends Comparable<Plugin> {

  /**
   * 插件优先级 越小优先级越高
   *
   * @return order
   */
  default int order() {
    return Integer.MAX_VALUE;
  }

  /**
   * 启动
   */
  default void start() {
  }

  /**
   * 停止
   */
  default void stop() {
  }

  /**
   * 使用order进行比较
   *
   * @param o compareTo
   * @return result
   */
  @Override
  default int compareTo(Plugin o) {
    return Integer.compare(order(), o.order());
  }
}
