package vip.justlive.oxygen.core.util;

/**
 * @author wubo
 */
public class Checks {

  private Checks() {
  }

  /**
   * 非空检查
   *
   * @param obj 校验值
   * @param <T> 泛型
   * @return 传入值
   */
  public static <T> T notNull(T obj) {
    return notNull(obj, "can not be null");
  }

  /**
   * 非空校验
   *
   * @param obj 校验值
   * @param msg 错误消息
   * @param <T> 泛型
   * @return 传入值
   */
  public static <T> T notNull(T obj, String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
    return obj;
  }
}
