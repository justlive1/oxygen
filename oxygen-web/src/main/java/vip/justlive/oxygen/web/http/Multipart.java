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
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.io.FileCleaner;

/**
 * multipart请求类型
 *
 * @author wubo
 */
@Getter
@ToString
@EqualsAndHashCode
public class Multipart implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * boundary
   */
  private final byte[] boundary;
  private final transient FileCleaner cleaner = new FileCleaner();
  private final transient Map<String, MultipartItem> data;

  public Multipart(String boundary, String encoding) {
    try {
      this.boundary = boundary.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      throw Exceptions.wrap(e);
    }
    this.data = new HashMap<>(4);
  }
}
