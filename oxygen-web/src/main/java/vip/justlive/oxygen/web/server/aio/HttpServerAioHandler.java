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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.Bytes;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.HttpHeaders.RequestStatusLine;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;
import vip.justlive.oxygen.core.util.net.aio.AioHandler;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.oxygen.core.util.net.http.HttpMethod;
import vip.justlive.oxygen.web.Context;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;
import vip.justlive.oxygen.web.router.RoutingContextImpl;

/**
 * http aio处理
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class HttpServerAioHandler implements AioHandler {

  private static final int MAX = 2048;
  private final String contextPath;

  @Override
  public ByteBuffer encode(Object data, ChannelContext channelContext) {
    Response response = (Response) data;
    Bytes bytes = new Bytes();
    // 状态行
    bytes.write(response.getRequest().getProtocol()).write(Bytes.SPACE)
        .write(Integer.toString(response.getStatus())).write(Bytes.CR).write(Bytes.LF);

    byte[] body = new byte[0];
    if (response.getOut() != null) {
      body = response.getOut().toByteArray();
    }

    String connection = response.getRequest().getHeader(HttpHeaders.CONNECTION);
    if (HttpHeaders.CONNECTION_KEEP_ALIVE.equalsIgnoreCase(connection)) {
      response.setHeader(HttpHeaders.CONNECTION, HttpHeaders.CONNECTION_KEEP_ALIVE);
    }
    // content-length
    response.setHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(body.length));
    // content-type
    if (response.getHeader(HttpHeaders.CONTENT_TYPE) == null && response.getContentType() != null) {
      String contentType = response.getContentType();
      if (!contentType.contains(HttpHeaders.CHARSET)) {
        contentType += Strings.SEMICOLON.concat(HttpHeaders.CHARSET).concat(Strings.EQUAL)
            .concat(response.getEncoding());
      }
      response.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }
    // 响应头
    response.getHeaders().forEach(
        (k, v) -> bytes.write(k).write(Bytes.COLON).write(Bytes.SPACE).write(v).write(Bytes.CR)
            .write(Bytes.LF));
    // cookies
    response.getCookies().values()
        .forEach(cookie -> bytes.write(cookie.toBytes()).write(Bytes.CR).write(Bytes.LF));

    bytes.write(Bytes.CR).write(Bytes.LF);

    // 响应体
    if (body.length > 0) {
      bytes.write(body);
      if (log.isDebugEnabled()) {
        if (body.length > MAX) {
          log.debug("http response [{}*mask*]", bytes.toString(MAX, StandardCharsets.UTF_8));
        } else {
          log.debug("http response [{}]", bytes);
        }
      }
    }
    return ByteBuffer.wrap(bytes.toArray());
  }

  @Override
  public Object decode(ByteBuffer buffer, int readableSize, ChannelContext channelContext) {
    int index = buffer.position();
    RequestStatusLine statusLine = HttpHeaders.parseRequestStatusLine(buffer);
    if (statusLine == null) {
      return null;
    }
    if (Strings.hasText(contextPath) && !statusLine.getRequestUri().startsWith(contextPath)) {
      throw Exceptions.fail(
          String.format("RequestUri [%s] not match contextPath [%s]", statusLine.getRequestUri(),
              contextPath));
    }
    Map<String, List<String>> headers = HttpHeaders.parseHttpHeaders(buffer);
    if (headers == null) {
      return null;
    }
    byte[] body = HttpHeaders.parseHttpBody(headers, buffer);
    if (body == null) {
      return null;
    }
    if (log.isDebugEnabled()) {
      log.debug("Received Http [{}]",
          new String(buffer.array(), index, buffer.position(), StandardCharsets.UTF_8));
    }
    Request request = new Request(HttpMethod.find(statusLine.getMethod()),
        statusLine.getRequestUri(), statusLine.getVersion(), contextPath, body);
    String[] arr = new String[0];
    headers.forEach((k, v) -> request.getHeaders().put(k, v.toArray(arr)));
    request.addAttribute(Request.ORIGINAL_REQUEST, channelContext);
    return request;
  }

  @Override
  public void handle(Object data, ChannelContext channelContext) {
    Request request = (Request) data;
    Response response = new Response(request);

    if (request.getMethod() == HttpMethod.UNKNOWN) {
      response.write("method is not supported");
      channelContext.write(response);
      return;
    }

    request.local();
    response.local();

    // trace-id
    String traceId = request.getHeader(Strings.TRACE_ID);
    if (!Strings.hasText(traceId)) {
      traceId = ThreadUtils.nextTraceId();
    }
    MDC.put(Strings.TRACE_ID, traceId);

    final RoutingContext ctx = new RoutingContextImpl(request, response);
    try {
      Context.dispatch(ctx);
    } catch (Exception e) {
      Context.routeError(ctx, e);
    } finally {
      Request.clear();
      Response.clear();
      channelContext.write(response);
      MDC.remove(Strings.TRACE_ID);
    }
  }
}