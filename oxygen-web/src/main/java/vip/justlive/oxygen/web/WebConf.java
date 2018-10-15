/*
 * Copyright (C) 2018 justlive1
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
package vip.justlive.oxygen.web;

import lombok.Data;
import vip.justlive.oxygen.core.config.Value;

/**
 * web相关配置
 *
 * @author wubo
 */
@Data
public class WebConf {

  /**
   * embedded 启动端口
   */
  @Value("${server.port:8080}")
  private Integer port;

  /**
   * context path
   */
  @Value("${server.contextPath:}")
  private String contextPath;

  /**
   * docBase
   */
  @Value("${server.docBase:src/main/webapp}")
  private String docBase;

  /**
   * 默认静态资源请求前缀
   */
  @Value("${web.static.default.prefix:/public}")
  private String defaultStaticPrefix;

  /**
   * 默认静态资源目录
   */
  @Value("${web.static.default.path:/public,/static,classpath:/META-INF/resources/webjars}")
  private String[] defaultStaticPaths;

  /**
   * 静态资源缓存时间
   */
  @Value("${web.static.cache:3600}")
  private Integer staticCache;

  /**
   * web jsp路径前缀
   */
  @Value("${web.view.jsp.prefix:/WEB-INF/}")
  private String jspPrefix;

}
