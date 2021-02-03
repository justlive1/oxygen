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
package vip.justlive.oxygen.web.http;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import vip.justlive.oxygen.core.util.base.Bytes;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * cookie
 *
 * @author wubo
 */
@Data
public class Cookie implements Serializable {

  private static final long serialVersionUID = 1L;
  /**
   * Cookie name
   */
  private String name;
  /**
   * Cookie domain
   */
  private String domain;
  /**
   * Cookie path
   */
  private String path;
  /**
   * Cookie value
   */
  private String value;
  /**
   * Cookie max-age in second
   */
  private Integer maxAge;
  /**
   * Secure ... e.g. use SSL
   */
  private boolean secure;

  public byte[] toBytes() {
    if (name == null || value == null) {
      return new byte[0];
    }
    Bytes bytes = new Bytes();
    bytes.write(HttpHeaders.SET_COOKIE).write(Bytes.COLON).write(Bytes.SPACE).write(name)
        .write(Strings.EQUAL).write(value);
    if (maxAge != null) {
      bytes.write("; Expires=").write(DateTimeFormatter.RFC_1123_DATE_TIME
          .format(ZonedDateTime.now(ZoneId.of("GMT")).plusSeconds(maxAge)));
      bytes.write("; Max-Age=").write(maxAge.toString());
    }
    if (Strings.hasText(domain)) {
      bytes.write("; Domain=").write(domain);
    }
    if (Strings.hasText(path)) {
      bytes.write("; Path=").write(path);
    }
    if (secure) {
      bytes.write("; Secure");
    }
    return bytes.toArray();
  }

}
