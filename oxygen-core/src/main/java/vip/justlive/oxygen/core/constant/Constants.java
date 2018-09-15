package vip.justlive.oxygen.core.constant;

/**
 * 常量类
 *
 * @author wubo
 */
public class Constants {

  /**
   * 配置文件默认地址
   */
  public static final String[] CONFIG_PATHS = new String[]{"classpath:/config/*.properties",
      "classpath:/*.properties"};
  /**
   * override配置文件地址属性key
   */
  public static final String CONFIG_OVERRIDE_PATH_KEY = "config.override.path";
  /**
   * ioc扫码路径属性key
   */
  public static final String IOC_SCAN_KEY = "main.ioc.scan";
  /**
   * vertx verticle路径key
   */
  public static final String VERTICLE_PATH_KEY = "main.verticle.path";
  /**
   * 成功code
   */
  public static final String SUCCESS_CODE = "00000";
  /**
   * 失败code
   */
  public static final String DEFAULT_FAIL_CODE = "99999";
  /**
   * 返回实体code属性字段
   */
  public static final String RESP_CODE_FIELD = "code";
  /**
   * 返回实体message属性字段
   */
  public static final String RESP_MESSAGE_FIELD = "message";
  /**
   * 返回实体success属性字段
   */
  public static final String RESP_IS_SUCCESS = "success";
  /**
   * 冒号
   */
  public static final String COLON = ":";
  /**
   * 逗号
   */
  public static final String COMMA = ",";
  /**
   * 点
   */
  public static final String DOT = ".";
  /**
   * 匹配所有
   */
  public static final String ANY = "*";
  /**
   * 匹配所有路径
   */
  public static final String ANY_PATH = "/*";
  /**
   * 根目录
   */
  public static final String ROOT_PATH = "/";
  /**
   * URL协议-文件
   */
  public static final String URL_PROTOCOL_FILE = "file";
  /**
   * URL协议-jar
   */
  public static final String URL_PROTOCOL_JAR = "jar";
  /**
   * URL协议-war
   */
  public static final String URL_PROTOCOL_WAR = "war";
  /**
   * URL协议-zip
   */
  public static final String URL_PROTOCOL_ZIP = "zip";
  /**
   * URL协议-WebSphere jar
   */
  public static final String URL_PROTOCOL_WSJAR = "wsjar";
  /**
   * URL协议-JBoss jar
   */
  public static final String URL_PROTOCOL_VFSZIP = "vfszip";
  /**
   * path分隔符
   */
  public static final String PATH_SEPARATOR = ROOT_PATH;
  /**
   * war路径分隔符
   */
  public static final String WAR_URL_SEPARATOR = "*/";
  /**
   * jar路径分隔符
   */
  public static final String JAR_URL_SEPARATOR = "!/";
  /**
   * classpath*
   */
  public static final String ALL_CLASSPATH_PREFIX = "classpath*:";
  /**
   * classpath
   */
  public static final String CLASSPATH_PREFIX = "classpath:";
  /**
   * file
   */
  public static final String FILE_PREFIX = "file:";

  /**
   * Email校验正则
   */
  public static final String REGEX_EMAIL = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";

  // 正则
  /**
   * 身份证校验正则
   */
  public static final String REGEX_IDCARD = "[1-9]\\d{13,16}[a-zA-Z0-9]{1}";
  /**
   * 二代身份证校验正则
   */
  public static final String REGEX_IDCARD2ND = "[1-9]\\d{16}[a-zA-Z0-9]{1}";
  /**
   * 手机校验正则
   */
  public static final String REGEX_MOBILE = "(\\+\\d+)?1[34578]\\d{9}$";
  /**
   * 电话校验正则
   */
  public static final String REGEX_PHONE = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";
  /**
   * 整数校验正则
   */
  public static final String REGEX_DIGIT = "[\\-\\+]?\\d+";
  /**
   * 空白字符校验正则
   */
  public static final String REGEX_BLANK_SPACE = "\\s+";
  /**
   * 中文字符校验正则
   */
  public static final String REGEX_CHINESE = "^[\u4E00-\u9FA5]+$";
  /**
   * 邮政编码校验正则
   */
  public static final String REGEX_POSTCODE = "[1-9]\\d{5}";
  /**
   * ipv4校验正则
   */
  public static final String REGEX_IP = "[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))";

  private Constants() {
  }
}
