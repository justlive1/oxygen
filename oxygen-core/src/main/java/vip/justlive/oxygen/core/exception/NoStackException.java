package vip.justlive.oxygen.core.exception;

/**
 * 不带堆栈的异常
 *
 * @author wubo
 */
public class NoStackException extends CodedException {

  private static final long serialVersionUID = 1L;

  /**
   * 构造方法
   *
   * @param errorCode 异常编码包装
   * @param args 参数
   */
  NoStackException(ErrorCode errorCode, Object... args) {
    super(errorCode, args);
  }

  /**
   * 覆盖该方法，以提高服务层异常Runtime时的执行效率
   * <br>
   * 不需要声明方法为父类的synchronized
   */
  @Override
  public Throwable fillInStackTrace() {
    return this;
  }
}
