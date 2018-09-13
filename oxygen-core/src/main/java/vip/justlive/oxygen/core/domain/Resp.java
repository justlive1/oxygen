package vip.justlive.oxygen.core.domain;

import lombok.Data;

/**
 * json返回实体
 *
 * @author wubo
 */
@Data
public class Resp {

  public static final String SUCC = "00000";
  public static final String FAIL = "99999";
  /**
   * 返回结果编码
   */
  private String code;

  /**
   * 结果描述信息
   */
  private String message;

  /**
   * 返回数据
   */
  private Object data;

  /**
   * 成功返回
   *
   * @return 返回实体
   */
  public static Resp success() {
    return success(null);
  }

  /**
   * 成功返回
   *
   * @param data 数据
   * @return 返回实体
   */
  public static Resp success(Object data) {
    Resp resp = new Resp();
    resp.setData(data);
    resp.setCode(SUCC);
    return resp;
  }

  /**
   * 失败返回
   *
   * @param message 消息
   * @return 返回实体
   */
  public static Resp error(String message) {
    return error(FAIL, message);
  }

  /**
   * 失败返回
   *
   * @param code 编码
   * @param message 消息
   * @return 返回实体
   */
  public static Resp error(String code, String message) {
    Resp resp = new Resp();
    resp.setCode(code);
    resp.setMessage(message);
    return resp;
  }

  /**
   * 是否成功
   *
   * @return 是否成功
   */
  public boolean isSuccess() {
    return SUCC.equals(code);
  }
}
