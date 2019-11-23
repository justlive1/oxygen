package vip.justlive.oxygen.core.util;

import java.util.Map;
import lombok.experimental.UtilityClass;

/**
 * http header
 *
 * @author wubo
 * @since 2.1.2
 */
@UtilityClass
public class HttpHeaders {

  public static final String BODY_STORE_KEY = "_body";
  public static final String BOUNDARY = "boundary";
  public static final String HTTP_PREFIX = "http://";
  public static final String HTTPS_PREFIX = "https://";
  public static final String MULTIPART = "multipart/";
  public static final String FORM_DATA = "form-data";
  public static final String FORM_DATA_NAME = "name";
  public static final String FORM_DATA_FILENAME = "filename";
  public static final String MAX_AGE = "max-age";

  public static final String CHUNKED = "chunked";
  public static final String CONNECTION_CLOSE = "close";
  public static final String CONNECTION_KEEP_ALIVE = "keep-alive";

  public static final String ACCEPT = "Accept";
  public static final String ACCEPT_CHARSET = "Accept-Charset";
  public static final String ACCEPT_ENCODING = "Accept-Encoding";
  public static final String ALLOW = "Allow";
  public static final String CACHE_CONTROL = "Cache-Control";
  public static final String CHARSET = "charset";
  public static final String CONNECTION = "Connection";
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
  public static final String LOCATION = "Location";
  public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
  public static final String SERVER = "Server";
  public static final String TRANSFER_ENCODING = "Transfer-Encoding";
  public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
  public static final String X_FORWARDED_FOR = "X-Forwarded-For";
  public static final String X_REAL_IP = "X-Real-IP";
  public static final String X_REQUESTED_WITH = "X-Requested-With";
  public static final String XML_HTTP_REQUEST = "XMLHttpRequest";
  public static final String SET_COOKIE = "Set-Cookie";

  public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
  public static final String APPLICATION_JSON = "application/json";
  public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
  public static final String APPLICATION_PDF = "application/pdf";
  public static final String APPLICATION_XML = "application/xml";
  public static final String MULTIPART_FORM_DATA = "multipart/form-data";
  public static final String TEXT_PLAIN = "text/plain";
  public static final String TEXT_XML = "text/xml";
  public static final String TEXT_HTML = "text/html";

  /**
   * 获取header
   *
   * @param name 属性名
   * @param headers headers
   * @return values
   */
  public static String[] getHeaders(String name, Map<String, String[]> headers) {
    if (headers == null || name == null) {
      return Strings.EMPTY_ARRAY;
    }
    String[] values = headers.get(name);
    if (values != null) {
      return values;
    }
    values = headers.get(name.toLowerCase());
    if (values != null) {
      return values;
    }
    for (Map.Entry<String, String[]> entry : headers.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(name)) {
        return entry.getValue();
      }
    }
    return Strings.EMPTY_ARRAY;
  }

  /**
   * 获取header
   *
   * @param name 属性名
   * @param headers headers
   * @return value
   */
  public static String getHeader(String name, Map<String, String[]> headers) {
    String[] values = getHeaders(name, headers);
    if (values.length > 0) {
      return values[0];
    }
    return null;
  }
}
