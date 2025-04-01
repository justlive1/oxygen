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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
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
  public final String USER_AGENT = "User-Agent";

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
   * @param name    属性名
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
   * @param name    属性名
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

  /**
   * 获取header
   *
   * @param name    属性名
   * @param headers headers
   * @return value
   */
  public String getSimpleHeader(String name, Map<String, String> headers) {
    if (headers == null || name == null) {
      return null;
    }
    String value = headers.get(name);
    if (value != null) {
      return value;
    }
    value = headers.get(name.toLowerCase());
    if (value != null) {
      return value;
    }
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(name)) {
        return entry.getValue();
      }
    }
    return null;
  }

  public RequestStatusLine parseRequestStatusLine(ByteBuffer buffer) {
    int position = buffer.position();
    String method = null;
    String requestUrl = null;
    byte[] data;
    while (buffer.hasRemaining()) {
      byte b = buffer.get();
      if (b == ' ') {
        int curr = buffer.position();
        data = new byte[curr - position - 1];
        getData(data, buffer, curr, position);
        position = curr;
        String line = new String(data, StandardCharsets.UTF_8);
        if (method == null) {
          method = line;
        } else if (requestUrl == null) {
          requestUrl = line;
        } else {
          break;
        }
      } else if (httpLineFinished(buffer, b)) {
        int curr = buffer.position();
        data = new byte[curr - position - 2];
        getData(data, buffer, curr, position);
        return new RequestStatusLine(method, requestUrl, new String(data, StandardCharsets.UTF_8));
      }
    }
    return null;
  }

  public ResponseStatusLine parseResponseStatusLine(ByteBuffer buffer) {
    int position = buffer.position();
    String version = null;
    String statusText = null;
    byte[] data;
    while (buffer.hasRemaining()) {
      byte b = buffer.get();
      if (b == ' ') {
        int curr = buffer.position();
        data = new byte[curr - position - 1];
        getData(data, buffer, curr, position);
        position = curr;
        String line = new String(data, StandardCharsets.UTF_8);
        if (version == null) {
          version = line;
        } else if (statusText == null) {
          statusText = line;
        } else {
          break;
        }
      } else if (httpLineFinished(buffer, b)) {
        int curr = buffer.position();
        data = new byte[curr - position - 2];
        getData(data, buffer, curr, position);
        String message = new String(data, StandardCharsets.UTF_8);
        if (statusText == null) {
          statusText = message;
          message = null;
        }
        return new ResponseStatusLine(version, statusText, message);
      }
    }
    return null;
  }

  public Map<String, List<String>> parseHttpHeaders(ByteBuffer buffer) {
    if (!buffer.hasRemaining()) {
      return null;
    }
    Map<String, List<String>> headers = new HashMap<>(4);
    int position = buffer.position();
    if (httpHeaderFinished(buffer)) {
      return headers;
    }
    String name = null;
    byte[] data;
    while (buffer.hasRemaining()) {
      byte b = buffer.get();
      int curr = buffer.position();
      if (name == null && b == Bytes.COLON) {
        data = new byte[curr - position - 1];
        getData(data, buffer, curr, position);
        name = new String(data, StandardCharsets.UTF_8);
        position = curr;
      } else if (name != null && httpLineFinished(buffer, b)) {
        data = new byte[curr - position - 2];
        getData(data, buffer, curr, position);
        addHeader(headers, name, new String(data, StandardCharsets.UTF_8).trim());
        name = null;
        position = curr;
        if (httpHeaderFinished(buffer)) {
          return headers;
        }
      }
    }
    // 数据包未读取完整
    return null;
  }

  public byte[] parseHttpBody(Map<String, List<String>> headers, ByteBuffer buffer) {
    String contentLength = getHeader(headers, CONTENT_LENGTH);
    if (contentLength != null) {
      int length = Integer.parseInt(contentLength);
      if (buffer.remaining() < length) {
        return null;
      }
      if (length == 0) {
        return null;
      }
      byte[] body = new byte[length];
      buffer.get(body);
      return body;
    }
    // 找不到contentLength当没有body处理
    return new byte[0];
  }

  public String getHeader(Map<String, List<String>> headers, String key) {
    for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(key)) {
        return entry.getValue().get(0);
      }
    }
    return null;
  }

  public void addHeader(Map<String, List<String>> headers, String key, String value) {
    List<String> values = headers.computeIfAbsent(key, k -> new ArrayList<>(1));
    if (!values.contains(value)) {
      values.add(value);
    }
  }

  public boolean httpHeaderFinished(ByteBuffer buffer) {
    return buffer.get() == Bytes.CR && buffer.hasRemaining() && buffer.get() == Bytes.LF;
  }

  public boolean httpLineFinished(ByteBuffer buffer, byte curr) {
    return curr == Bytes.LF && buffer.get(buffer.position() - 2) == Bytes.CR;
  }

  public void getData(byte[] data, ByteBuffer buffer, int curr, int index) {
    buffer.position(index);
    buffer.get(data);
    buffer.position(curr);
  }

  @Data
  public static class RequestStatusLine {

    private final String method;
    private final String requestUri;
    private final String version;
  }

  @Data
  public static class ResponseStatusLine {

    private final String version;
    private final String statusText;
    private final String message;
  }
}
