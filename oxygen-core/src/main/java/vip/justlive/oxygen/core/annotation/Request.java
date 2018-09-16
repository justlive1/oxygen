package vip.justlive.oxygen.core.annotation;

/**
 * route的请求映射
 *
 * @author wubo
 */
public @interface Request {

  /**
   * 地址映射 (e.g. "/myPath.do").
   *
   * @return 请求地址
   */
  String[] value() default {};

  /**
   * HTTP请求类型 :GET, POST, HEAD, OPTIONS, PUT, PATCH, DELETE, TRACE.
   *
   * @return 请求类型
   */
  String[] method() default {};

  /**
   * 指定处理请求的提交内容类型(Content-Type) *
   *
   * <pre class="code">
   * consumes = "text/plain"
   * consumes = {"text/plain", "application/*"}
   * </pre>
   *
   * @return Content-Type
   */
  String[] consumes() default {};

  /**
   * 指定返回的内容类型，仅当request请求头中的(Accept)类型中包含该指定类型才返回
   *
   *
   * <pre class="code">
   * produces = "text/plain"
   * produces = {"text/plain", "application/*"}
   * produces = "application/json; charset=UTF-8"
   * </pre>
   *
   * @return Accept
   */
  String[] produces() default {};
}
