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
package vip.justlive.oxygen.web;

import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.config.ConfigKey;

/**
 * web相关配置
 *
 * @author wubo
 */
@UtilityClass
public class WebConfigKeys {

  /**
   * embedded 启动端口
   */
  public ConfigKey SERVER_PORT = new ConfigKey("oxygen.server.port");

  /**
   * context path
   */
  public ConfigKey SERVER_CONTEXT_PATH = new ConfigKey("oxygen.server.contextPath", "");

  /**
   * request默认编码
   */
  public ConfigKey REQUEST_CHARSET = new ConfigKey("oxygen.web.request.charset", "utf-8");

  /**
   * 是否处理options请求，默认不返回数据
   */
  public ConfigKey REQUEST_HANDLE_OPTIONS = new ConfigKey("oxygen.web.request.handleOptions",
      "false");

  /**
   * session失效时间，单位秒
   */
  public ConfigKey SESSION_EXPIRED = new ConfigKey("oxygen.web.session.expired", "3600");

  /**
   * 默认静态资源请求前缀
   */
  public ConfigKey STATIC_PREFIX = new ConfigKey("oxygen.web.static.prefix", "/public");

  /**
   * 默认静态资源目录
   */
  public ConfigKey STATIC_PATH = new ConfigKey("oxygen.web.static.path",
      "/public,/static,classpath:/META-INF/resources/webjars");

  /**
   * 静态资源缓存时间
   */
  public ConfigKey STATIC_CACHE = new ConfigKey("oxygen.web.static.cache", "3600");

  /**
   * web jsp路径前缀
   */
  public ConfigKey VIEW_PREFIX_JSP = new ConfigKey("oxygen.web.view.jsp.prefix", "WEB-INF");

  /**
   * thymeleaf 路径前缀
   */
  public ConfigKey VIEW_PREFIX_THYMELEAF = new ConfigKey("oxygen.web.view.thymeleaf.prefix",
      "/templates");

  /**
   * thymeleaf 视图后缀
   */
  public ConfigKey VIEW_SUFFIX_THYMELEAF = new ConfigKey("oxygen.web.view.thymeleaf.suffix",
      ".html");

  /**
   * freemarker 路径前缀
   */
  public ConfigKey VIEW_PREFIX_FREEMARKER = new ConfigKey("oxygen.web.view.freemarker.prefix",
      "/templates");

  /**
   * simple 路径前缀
   */
  public ConfigKey VIEW_PREFIX_SIMPLE = new ConfigKey("oxygen.web.view.simple.prefix",
      "/templates");

  /**
   * simple 视图后缀
   */
  public ConfigKey VIEW_SUFFIX_SIMPLE = new ConfigKey("oxygen.web.view.simple.suffix", ".htm");

  /**
   * view cache
   */
  public ConfigKey VIEW_CACHE = new ConfigKey("oxygen.web.view.cache.enabled", "true");

}
