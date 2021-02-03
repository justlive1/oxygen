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
package vip.justlive.oxygen.web.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.util.Headers;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletException;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.web.WebConfigKeys;
import vip.justlive.oxygen.web.server.WebServer;
import vip.justlive.oxygen.web.servlet.ServletWebInitializer;

/**
 * undertow server
 *
 * @author wubo
 */
public class UndertowWebServer implements WebServer {

  private Undertow undertow;
  private int port;

  private UndertowConf undertowConf;
  private String contextPath;

  @Override
  public void listen(int port) {
    this.port = port;
    this.contextPath = WebConfigKeys.SERVER_CONTEXT_PATH.getValue();
    undertowConf = ConfigFactory.load(UndertowConf.class);
    DeploymentManager manager = deployment();
    manager.deploy();
    Undertow.Builder builder = Undertow.builder();
    configServer(builder);
    try {
      undertow = builder.setHandler(configHttp(manager.start())).build();
    } catch (ServletException e) {
      throw Exceptions.wrap(e);
    }
    undertow.start();
  }

  @Override
  public void stop() {
    if (undertow != null) {
      undertow.stop();
    }
  }

  @Override
  public int getPort() {
    return this.port;
  }

  private DeploymentManager deployment() {
    Set<Class<?>> handlesTypes = new HashSet<>(2);
    return Servlets.defaultContainer().addDeployment(
        Servlets.deployment().setClassLoader(ClassUtils.getDefaultClassLoader())
            .setContextPath(contextPath).setDeploymentName("oxygen")
            .addServletContainerInitializer(
                new ServletContainerInitializerInfo(ServletWebInitializer.class, handlesTypes)));
  }

  private void configServer(Undertow.Builder builder) {
    builder.addHttpListener(this.port, undertowConf.getHost())
        .setWorkerThreads(undertowConf.getWorkerThreads()).setIoThreads(undertowConf.getIoThreads())
        .setServerOption(UndertowOptions.ALLOW_UNESCAPED_CHARACTERS_IN_URL,
            undertowConf.isAllowUnescapedCharactersInUrl())
        .setServerOption(UndertowOptions.ENABLE_HTTP2, undertowConf.isHttp2enabled());
  }

  private HttpHandler configHttp(HttpHandler httpHandler) {
    HttpHandler handler = httpHandler;
    if (Strings.hasText(contextPath)) {
      handler = Handlers.path().addPrefixPath(contextPath, httpHandler);
    }
    return new EncodingHandler(handler, new ContentEncodingRepository()
        .addEncodingHandler("gzip", new GzipEncodingProvider(undertowConf.getGzipLevel()),
            undertowConf.getGzipPriority(), this::gzipEnabled));
  }

  private boolean gzipEnabled(final HttpServerExchange value) {
    if (undertowConf.isGzipEnabled()) {
      final String length = value.getResponseHeaders().getFirst(Headers.CONTENT_LENGTH);
      if (length == null) {
        return false;
      }
      return Long.parseLong(length) > undertowConf.getGzipMinLength();
    }
    return false;
  }
}
