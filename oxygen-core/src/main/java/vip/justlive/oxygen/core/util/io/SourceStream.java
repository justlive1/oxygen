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
package vip.justlive.oxygen.core.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 各种类型的输入公共接口
 *
 * @author wubo
 */
public interface SourceStream {

  /**
   * 获取输入流
   *
   * @return 输入流
   * @throws IOException io异常
   */
  InputStream getInputStream() throws IOException;
}
