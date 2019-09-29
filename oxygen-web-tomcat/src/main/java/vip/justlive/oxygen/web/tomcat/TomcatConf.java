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
package vip.justlive.oxygen.web.tomcat;

import java.nio.charset.StandardCharsets;
import lombok.Data;
import org.apache.catalina.valves.Constants.AccessLog;
import vip.justlive.oxygen.core.config.ValueConfig;
import vip.justlive.oxygen.core.util.Strings;

/**
 * tomcat配置
 *
 * @author wubo
 */
@Data
@ValueConfig("server.tomcat")
public class TomcatConf {

  /**
   * 后缀名jar
   */
  static final String JAR_EXT = ".jar";

  /**
   * META-INF
   */
  static final String META_INF = "META-INF";
  /**
   * /META-INF
   */
  static final String META_INF_PATH = Strings.SLASH + META_INF;
  /**
   * META-INF/resources
   */
  static final String META_INF_RESOURCES = "META-INF/resources";
  /**
   * /META-INF/resources
   */
  static final String META_INF_RESOURCES_PATH = Strings.SLASH + META_INF_RESOURCES;
  /**
   * WEB-INF
   */
  static final String WEB_INF = "WEB-INF";
  /**
   * /WEB-INF/lib/
   */
  static final String WEB_INF_LIB = "/WEB-INF/lib/";
  /**
   * /WEB-INF/classes
   */
  static final String WEB_INF_CLASSES = "/WEB-INF/classes";

  // protocolHandler

  /**
   * Maximum queue length for incoming connection requests when all possible request processing
   * threads are in use.
   */
  private int acceptCount = 100;
  /**
   * Maximum number of connections that the server will accept and process at any given time.
   */
  private int maxConnections = 5000;
  /**
   * Maximum amount of worker threads.
   */
  private int maxThreads = 200;
  /**
   * Minimum amount of worker threads.
   */
  private int minSpareThreads = 10;
  /**
   * Maximum size in bytes of the HTTP message header.
   */
  private int maxHttpHeaderSize = 8 * 1024;
  /**
   * Maximum size in bytes of the HTTP post content.
   */
  private int maxHttpPostSize = 2 * 1024 * 1024;
  /**
   * time out
   */
  private int connectionTimeout = 20000;
  /**
   * Character encoding to use to decode the URI.
   */
  private String uriEncoding = StandardCharsets.UTF_8.name();

  // engine

  /**
   * Delay in seconds between the invocation of backgroundProcess methods.
   */
  private int backgroundProcessorDelay = 10;

  //access log

  /**
   * Enable access log.
   */
  private boolean accessLogEnabled = false;
  /**
   * Whether to buffer output such that it is flushed only periodically.
   */
  private boolean accessLogBuffered = true;
  /**
   * Set request attributes for the IP address, Hostname, protocol, and port used for the request.
   */
  private boolean accessLogRequestAttributesEnabled = false;
  /**
   * Date format to place in the log file name.
   */
  private String accessLogFileFormat = ".yyyy-MM-dd";
  /**
   * Format pattern for access logs.
   */
  private String accessLogPattern = AccessLog.COMMON_ALIAS;

}
