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
package vip.justlive.oxygen.core.util.net.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.util.base.HttpHeaders;

/**
 * http body 类型
 *
 * @author wubo
 * @since 3.0.4
 */
@Getter
@RequiredArgsConstructor
public enum HttpBody {
  /**
   * 没有body
   */
  NONE(null),
  /**
   * 表单
   */
  FORM(HttpHeaders.APPLICATION_FORM_URLENCODED),
  /**
   * 复合类型
   */
  MULTIPART(HttpHeaders.MULTIPART_FORM_DATA),
  /**
   * json类型
   */
  JSON(HttpHeaders.APPLICATION_JSON),
  /**
   * 其他类型
   */
  OTHERS(null);

  private final String media;
}
