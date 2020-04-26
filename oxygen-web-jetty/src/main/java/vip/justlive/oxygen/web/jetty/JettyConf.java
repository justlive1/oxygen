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
package vip.justlive.oxygen.web.jetty;

import lombok.Data;
import vip.justlive.oxygen.core.config.ValueConfig;

/**
 * jetty config
 *
 * @author wubo
 */
@Data
@ValueConfig("server.jetty")
public class JettyConf {

  /**
   * 虚拟主机
   */
  private String[] virtualHosts;
  /**
   * 连接器在空闲状态持续该时间后（单位毫秒）关闭
   */
  private long idleTimeout = 30000L;
  /**
   * 温和的停止一个连接器前等待的时间（毫秒）
   */
  private long stopTimeout = 30000L;
  /**
   * 等待处理的连接队列大小
   */
  private int acceptQueueSize;
  /**
   * 允许Server socket被重绑定，即使在TIME_WAIT状态
   */
  private boolean reuseAddress = true;
  /**
   * 是否启用servlet3.0特性
   */
  private boolean configurationDiscovered = true;
  /**
   * 最大表单数据大小
   */
  private int maxFormContentSize = 256 * 1024 * 1024;
  /**
   * 最大表单键值对数量
   */
  private int maxFormKeys = 200;
}
