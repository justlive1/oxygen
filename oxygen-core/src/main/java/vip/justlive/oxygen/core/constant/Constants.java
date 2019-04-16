/*
 * Copyright (C) 2018 justlive1
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
package vip.justlive.oxygen.core.constant;

/**
 * 常量类
 *
 * @author wubo
 */
public class Constants {

  // settings

  /**
   * 配置文件默认地址
   */
  public static final String[] CONFIG_PATHS = {"classpath*:config.properties",
      "classpath*:/config/*.properties"};

  /**
   * 默认模块
   */
  public static final String DEFAULT_NODULE = "ERROR";
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
   * unknown
   */
  public static final String UNKNOWN = "unknown";
  /**
   * The Carriage Return ASCII character value.
   */
  public static final byte CR = 0x0D;

  /**
   * The Line Feed ASCII character value.
   */
  public static final byte LF = 0x0A;
  /**
   * The dash (-) ASCII character value.
   */
  public static final byte DASH = 0x2D;
  /**
   * 空字符串
   */
  public static final String EMPTY = "";
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
   * 分号
   */
  public static final String SEMICOLON = ";";
  /**
   * 等于
   */
  public static final String EQUAL = "=";
  /**
   * 问号
   */
  public static final String QUESTION_MARK = "?";
  /**
   * 双引号
   */
  public static final String DOUBLE_QUOTATION_MARK = "\"";
  /**
   * 与
   */
  public static final String AND = "&";
  /**
   * 斜线
   */
  public static final String SLASH = "/";
  /**
   * 连字符
   */
  public static final String HYPHEN = "-";
  /**
   * 下划线
   */
  public static final String UNDERSCORE = "_";
  /**
   * #
   */
  public static final String OCTOTHORP = "#";
  /**
   * 八进制前缀
   */
  public static final String OCTONARY_PREFIX = "0";
  /**
   * 十六进制前缀
   */
  public static final String HEX_PREFIX_0 = "0x";
  /**
   * 十六进制前缀
   */
  public static final String HEX_PREFIX_1 = "0X";
  /**
   * 数组类名前缀: "[]"
   */
  public static final String ARRAY_SUFFIX = "[]";
  /**
   * 内部数组类名前缀: "["
   */
  public static final String INTERNAL_ARRAY_PREFIX = "[";
  /**
   * 内部非基本数组类名前缀: "[L"
   */
  public static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";
  /**
   * 内部类分隔符: '$'
   */
  public static final char INNER_CLASS_SEPARATOR = '$';
  /**
   * CGLIB分隔符: "$$"
   */
  public static final String CGLIB_CLASS_SEPARATOR = "$$";
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
  public static final String ROOT_PATH = SLASH;
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
  public static final String PATH_SEPARATOR = SLASH;
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
   * 后缀名jar
   */
  public static final String JAR_EXT = ".jar";

  /**
   * META-INF
   */
  public static final String META_INF = "META-INF";
  /**
   * /META-INF
   */
  public static final String META_INF_PATH = "/META-INF";
  /**
   * META-INF/resources
   */
  public static final String META_INF_RESOURCES = "META-INF/resources";
  /**
   * /META-INF/resources
   */
  public static final String META_INF_RESOURCES_PATH = "/META-INF/resources";
  /**
   * WEB-INF
   */
  public static final String WEB_INF = "WEB-INF";
  /**
   * /WEB-INF/lib/
   */
  public static final String WEB_INF_LIB = "/WEB-INF/lib/";
  /**
   * /WEB-INF/classes
   */
  public static final String WEB_INF_CLASSES = "/WEB-INF/classes";
  /**
   * session cookie key
   */
  public static final String SESSION_COOKIE_KEY = "OX_SESSION";

  // http

  public static final int NOT_FOUND = 404;
  public static final int SERVER_ERROR = 500;

  public static final String ETAG_DATA_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
  public static final String BODY_STORE_KEY = "_body";
  public static final String BOUNDARY = "boundary";
  public static final String HTTP_PREFIX = "http://";
  public static final String HTTPS_PREFIX = "https://";
  public static final String MULTIPART = "multipart/";
  public static final String FORM_DATA = "form-data";
  public static final String FORM_DATA_NAME = "name";
  public static final String FORM_DATA_FILENAME = "filename";
  public static final String MAX_AGE = "max-age";


  public static final String ACCEPT = "Accept";
  public static final String ACCEPT_CHARSET = "Accept-Charset";
  public static final String ACCEPT_ENCODING = "Accept-Encoding";
  public static final String CACHE_CONTROL = "Cache-Control";
  public static final String CHARSET = "charset";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String CONTENT_DISPOSITION = "Content-disposition";
  public static final String CONTENT_LENGTH = "Content-length";
  public static final String COOKIE = "Cookie";
  public static final String ETAG = "ETag";
  public static final String EXPIRES = "Expires";
  public static final String HOST_NAME = "Host";
  public static final String IF_MATCH = "If-Match";
  public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
  public static final String IF_NONE_MATCH = "If-None-Match";
  public static final String LAST_MODIFIED = "Last-Modified";
  public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
  public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
  public static final String X_FORWARDED_FOR = "X-Forwarded-For";
  public static final String X_REAL_IP = "X-Real-IP";
  public static final String X_REQUESTED_WITH = "X-Requested-With";
  public static final String XML_HTTP_REQUEST = "XMLHttpRequest";


  public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
  public static final String APPLICATION_JSON = "application/json";
  public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
  public static final String APPLICATION_PDF = "application/pdf";
  public static final String APPLICATION_XML = "application/xml";
  public static final String MULTIPART_FORM_DATA = "multipart/form-data";
  public static final String TEXT_PLAIN = "text/plain";
  public static final String TEXT_XML = "text/xml";
  public static final String TEXT_HTML = "text/html";

  // 正则

  /**
   * Email校验正则
   */
  public static final String REGEX_EMAIL = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
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

  public static final String REGEX_PATH_VAR = "\\{\\w+\\}";
  public static final String REGEX_PATH_VAR_REPLACE = "(\\\\w+)";

  private Constants() {
  }
}
