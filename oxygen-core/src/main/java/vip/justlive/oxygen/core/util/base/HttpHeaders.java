/*
 * Copyright (C) 2020 the original author or authors.
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

package vip.justlive.oxygen.core.util.base;

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

  public final String BODY_STORE_KEY = "_body";
  public final String BOUNDARY = "boundary";
  public final String HTTP_PREFIX = "http://";
  public final String HTTPS_PREFIX = "https://";
  public final String MULTIPART = "multipart/";
  public final String FORM_DATA = "form-data";
  public final String FORM_DATA_NAME = "name";
  public final String FORM_DATA_FILENAME = "filename";
  public final String MAX_AGE = "max-age";

  public final String CHUNKED = "chunked";
  public final String CONNECTION_CLOSE = "close";
  public final String CONNECTION_KEEP_ALIVE = "keep-alive";

  public final String ACCEPT = "Accept";
  public final String ACCEPT_CHARSET = "Accept-Charset";
  public final String ACCEPT_ENCODING = "Accept-Encoding";
  public final String ALLOW = "Allow";
  public final String CACHE_CONTROL = "Cache-Control";
  public final String CHARSET = "charset";
  public final String CONNECTION = "Connection";
  public final String CONTENT_TYPE = "Content-Type";
  public final String CONTENT_DISPOSITION = "Content-disposition";
  public final String CONTENT_LENGTH = "Content-length";
  public final String COOKIE = "Cookie";
  public final String ETAG = "ETag";
  public final String EXPIRES = "Expires";
  public final String HOST_NAME = "Host";
  public final String IF_MATCH = "If-Match";
  public final String IF_MODIFIED_SINCE = "If-Modified-Since";
  public final String IF_NONE_MATCH = "If-None-Match";
  public final String LAST_MODIFIED = "Last-Modified";
  public final String LOCATION = "Location";
  public final String PROXY_CLIENT_IP = "Proxy-Client-IP";
  public final String SERVER = "Server";
  public final String TRANSFER_ENCODING = "Transfer-Encoding";
  public final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
  public final String X_FORWARDED_FOR = "X-Forwarded-For";
  public final String X_REAL_IP = "X-Real-IP";
  public final String X_REQUESTED_WITH = "X-Requested-With";
  public final String XML_HTTP_REQUEST = "XMLHttpRequest";
  public final String SET_COOKIE = "Set-Cookie";

  public final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
  public final String APPLICATION_JSON = "application/json";
  public final String APPLICATION_OCTET_STREAM = "application/octet-stream";
  public final String APPLICATION_PDF = "application/pdf";
  public final String APPLICATION_XML = "application/xml";
  public final String MULTIPART_FORM_DATA = "multipart/form-data";
  public final String MULTIPART_FORM_DATA_BOUNDARY = "multipart/form-data; boundary=";
  public final String TEXT_PLAIN = "text/plain";
  public final String TEXT_XML = "text/xml";
  public final String TEXT_HTML = "text/html";

  /**
   * 获取header
   *
   * @param name 属性名
   * @param headers headers
   * @return values
   */
  public String[] getHeaders(String name, Map<String, String[]> headers) {
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
  public String getHeader(String name, Map<String, String[]> headers) {
    String[] values = getHeaders(name, headers);
    if (values.length > 0) {
      return values[0];
    }
    return null;
  }
}
