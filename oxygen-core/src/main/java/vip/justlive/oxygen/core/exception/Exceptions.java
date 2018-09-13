package vip.justlive.oxygen.core.exception;

/**
 * 用于创建CodedException
 * <p>
 * fail方法创建的异常为业务逻辑异常，不含堆栈信息； fault方法创建的异常为故障型异常，包含堆栈信息
 * </p>
 *
 * @author wubo
 */
public class Exceptions {

  private Exceptions() {
  }

  /**
   * 抛出unchecked异常
   *
   * @param e 异常
   * @return 包装异常
   */
  public static CodedException wrap(Throwable e) {
    return new CodedException(e, null);
  }

  /**
   * 抛出unchecked异常
   *
   * @param e 异常
   * @param code 异常编码
   * @param message 异常消息
   * @return 包装异常
   */
  public static CodedException wrap(Throwable e, String code, String message) {
    return new CodedException(e, errorMessage(null, code, message));
  }

  /**
   * 抛出unchecked异常
   *
   * @param e 异常
   * @param errorCode 异常编码包装
   * @param arguments 参数
   * @return 包装异常
   */
  public static CodedException wrap(Throwable e, ErrorCode errorCode, Object... arguments) {
    return new CodedException(e, errorCode, arguments);
  }

  /**
   * 创建ErrorCode
   *
   * @param module 模块
   * @param code 编码
   * @return 异常编码包装
   */
  public static ErrorCode errorCode(String module, String code) {
    return new ErrorCode(module, code);
  }

  /**
   * 创建带错误提示信息的ErrorCode
   *
   * @param module 模块
   * @param code 编码
   * @param message 消息
   * @return 异常编码包装
   */
  public static ErrorCode errorMessage(String module, String code, String message) {
    return new ErrorCode(module, code, message);
  }

  /**
   * 创建可带参数的业务逻辑异常
   *
   * @param errCode 异常编码包装
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fail(ErrorCode errCode, Object... params) {
    return new NoStackException(errCode, params);
  }

  /**
   * 创建可带参数的业务逻辑异常
   *
   * @param code 异常编码
   * @param message 异常消息
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fail(String code, String message, Object... params) {
    return fail(errorMessage(null, code, message), params);
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param errCode 异常编码包装
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fault(ErrorCode errCode, Object... params) {
    return new CodedException(errCode, params);
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param e 异常
   * @param errCode 异常编码包装
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fault(Throwable e, ErrorCode errCode, Object... params) {
    return new CodedException(e, errCode, params);
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param code 异常编码
   * @param message 异常消息
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fault(String code, String message, Object... params) {
    return fault(errorMessage(null, code, message), params);
  }

  /**
   * 创建可带参数的故障异常
   *
   * @param e 异常
   * @param code 异常编码
   * @param message 异常消息
   * @param params 参数
   * @return 包装异常
   */
  public static CodedException fault(Throwable e, String code, String message, Object... params) {
    return fault(e, errorMessage(null, code, message), params);
  }
}
