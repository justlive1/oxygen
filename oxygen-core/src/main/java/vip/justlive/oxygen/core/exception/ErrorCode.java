package vip.justlive.oxygen.core.exception;

import java.io.Serializable;
import lombok.Data;

/**
 * 异常包装类
 *
 * @author wubo
 */
@Data
public class ErrorCode implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 模块
   */
  private String module;

  /**
   * 错误编码
   */
  private String code;

  /**
   * 错误信息
   */
  private String message;

  protected ErrorCode(String module, String code) {
    this.module = module;
    this.code = code;
  }

  protected ErrorCode(String module, String code, String message) {
    this.module = module;
    this.code = code;
    this.message = message;
  }

  /**
   * 获取module + code
   *
   * @return
   */
  public String getModuleCode() {
    if (module == null) {
      return code;
    } else {
      return module.concat(code);
    }
  }

  @Override
  public String toString() {
    return String.format("[%s][%s][%s]", module, code, message);
  }
}
