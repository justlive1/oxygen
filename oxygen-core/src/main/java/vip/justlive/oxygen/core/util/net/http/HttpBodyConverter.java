/*
 * Copyright (C) 2024 the original author or authors.
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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import vip.justlive.oxygen.core.Order;

/**
 * http body 转换期
 *
 * @author wubo
 * @since 3.0.10
 */
public interface HttpBodyConverter extends Order {

  /**
   * 测试是否可以写入
   *
   * @param request 请求
   * @return true则可以写入
   */
  boolean canWrite(HttpRequest request);

  /**
   * 写入
   *
   * @param request 请求
   * @param out     输出流
   * @throws IOException 异常
   */
  void write(HttpRequest request, OutputStream out) throws IOException;

  /**
   * 测试是否可以读取
   *
   * @param response 响应
   * @param type     类型
   * @return true则可以读取
   */
  default boolean canRead(HttpResponse response, Type type) {
    return false;
  }

  /**
   * 读取
   *
   * @param response 响应
   * @param type     类型
   * @param <T>      泛型
   * @return result
   */
  default <T> T read(HttpResponse response, Type type) {
    throw new UnsupportedOperationException();
  }
}
