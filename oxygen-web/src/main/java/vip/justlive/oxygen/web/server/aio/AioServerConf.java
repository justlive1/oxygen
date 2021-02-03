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

package vip.justlive.oxygen.web.server.aio;

import lombok.Data;
import vip.justlive.oxygen.core.config.ValueConfig;

/**
 * aio server配置
 *
 * @author wubo
 */
@Data
@ValueConfig("oxygen.server.aio")
public class AioServerConf {

  /**
   * aio服务连接空闲超时时间
   */
  private long idleTimeout = 10000;

  /**
   * aio请求连接超时时间
   */
  private long requestTimeout = -1;

  /**
   * 连接线程数
   */
  private int acceptThreads = 100;

  /**
   * 连接线程最大等待数
   */
  private int acceptMaxWaiter = 10000;

  /**
   * 工作线程数
   */
  private int workerThreads = 200;

  /**
   * 工作线程最大等待数
   */
  private int workerMaxWaiter = 1000000;

  /**
   * 是否hold住端口，true的话随主线程退出而退出，false的话则要主动退出
   */
  private boolean daemon;
}
