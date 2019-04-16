/*
 * Copyright (C) 2019 justlive1
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
package vip.justlive.oxygen.web.undertow;

import lombok.Data;
import vip.justlive.oxygen.core.config.ValueConfig;

/**
 * undertow配置
 *
 * @author wubo
 */
@Data
@ValueConfig("server.undertow")
public class UndertowConf {

  /**
   * 主机名
   */
  private String host = "0.0.0.0";
  /**
   * io线程数
   */
  private Integer ioThreads = Math.max(Runtime.getRuntime().availableProcessors(), 2);
  /**
   * worker线程数
   */
  private Integer workerThreads = ioThreads * 8;
  /**
   * 是否开启gzip压缩
   */
  private boolean gzipEnabled = false;
  /**
   * gzip处理优先级
   */
  private int gzipPriority = 100;
  /**
   * 压缩级别，默认值 -1。 可配置 1 到 9。 1 拥有最快压缩速度，9 拥有最高压缩率
   */
  private Integer gzipLevel = -1;
  /**
   * 触发压缩的最小内容长度
   */
  private Integer gzipMinLength = 1024;
  /**
   * url是否允许特殊字符
   */
  private boolean allowUnescapedCharactersInUrl = true;
  /**
   * 是否开启http2
   */
  private boolean http2enabled = false;

}
