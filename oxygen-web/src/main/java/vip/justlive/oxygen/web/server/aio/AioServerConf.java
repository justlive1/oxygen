/*
 * Copyright (C) 2019 the original author or authors.
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
import vip.justlive.oxygen.core.config.Value;

/**
 * aio server配置
 *
 * @author wubo
 */
@Data
public class AioServerConf {

  /**
   * aio服务连接空闲超时时间
   */
  @Value("${server.aio.idleTimeout:10000}")
  private long aioIdleTimeout;

  /**
   * aio请求连接超时时间
   */
  @Value("${server.aio.requestTimeout:-1}")
  private long aioRequestTimeout;

  /**
   * 连接线程数
   */
  @Value("${server.aio.acceptThreads:100}")
  private int acceptThreads = 100;

  /**
   * 连接线程最大等待数
   */
  @Value("${server.aio.acceptMaxWaiter:10000}")
  private int acceptMaxWaiter;

  /**
   * 工作线程数
   */
  @Value("${server.aio.workerThreads:200}")
  private int workerThreads;

  /**
   * 工作线程最大等待数
   */
  @Value("${server.aio.workerMaxWaiter:1000000}")
  private int workerMaxWaiter = 1000000;

  /**
   * 是否hold住端口，true的话随主线程退出而退出，false的话则要主动退出
   */
  @Value("${server.aio.daemon:false}")
  private boolean daemon;
}
