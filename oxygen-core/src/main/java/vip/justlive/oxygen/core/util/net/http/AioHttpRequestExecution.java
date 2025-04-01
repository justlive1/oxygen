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
package vip.justlive.oxygen.core.util.net.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.Bytes;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.HttpHeaders.ResponseStatusLine;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.base.Urls;
import vip.justlive.oxygen.core.util.net.aio.AioHandler;
import vip.justlive.oxygen.core.util.net.aio.AioListener;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.oxygen.core.util.net.aio.Client;
import vip.justlive.oxygen.core.util.net.aio.GroupContext;

/**
 * Aio 实现的执行器
 *
 * @author wubo
 * @since 3.0.10
 */
@Slf4j
public class AioHttpRequestExecution implements HttpRequestExecution {

  public static final AioHttpRequestExecution AIO = new AioHttpRequestExecution();
  private static final Client CLIENT;

  static {
    AioHandlerListener handlerListener = new AioHandlerListener();
    GroupContext context = new GroupContext(handlerListener);
    ConfigFactory.load(context, "oxygen.httpRequest.aio");
    context.setAioListener(handlerListener);
    context.setDaemon(true);
    CLIENT = new Client(context);
  }

  @Override
  public HttpResponse execute(HttpRequest request) throws IOException {
    String httpUrl = Urls.urlWithQueryString(request);
    URI uri = URI.create(httpUrl);
    if (log.isDebugEnabled()) {
      log.debug("Url [{}] parsed as [{}][{}:{}]", httpUrl, uri.getScheme(), uri.getHost(),
          uri.getPort());
    }
    if ("https".equals(uri.getScheme())) {
      return HucHttpRequestExecution.HUC.execute(request);
    }
    request.addHeader(HttpHeaders.HOST_NAME, uri.getHost());
    if (!request.getHeaders().containsKey(HttpHeaders.USER_AGENT)) {
      request.addHeader(HttpHeaders.USER_AGENT, "aio_client");
    }
    if (!request.getHeaders().containsKey(HttpHeaders.CONNECTION)) {
      request.addHeader(HttpHeaders.CONNECTION, HttpHeaders.CONNECTION_KEEP_ALIVE);
    }

    ChannelContext context = getChannel(uri);
    CompletableFuture<AioHttpResponse> future = new CompletableFuture<>();
    context.addAttr(AioHttpRequestExecution.class.getName(), future);

    waitChannelConnected(request, context);
    Bytes bytes = new Bytes();
    // add method
    bytes.write(request.getMethod().name()).write(Bytes.SPACE);
    // add path
    String path = uri.getRawPath();
    if (request.getQueryParam() != null) {
      path = path + Strings.QUESTION_MARK + uri.getRawQuery();
    }
    bytes.write(path).write(Bytes.SPACE).write("HTTP/1.1").write(Bytes.CR).write(Bytes.LF);

    boolean nonOutput = request.getMethod() == HttpMethod.GET || (request.getBody() == null
        && request.getHttpBody() != HttpBody.MULTIPART);
    // body
    byte[] body = null;
    if (!nonOutput) {
      HttpBodyConverter converter = HttpBodyConverters.findCanWrite(request);
      if (converter == null) {
        log.error("No HttpBodyConverter for Request {}", request);
        throw Exceptions.fail("No HttpBodyConverter for HttpRequest");
      }
      try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
        converter.write(request, stream);
        body = stream.toByteArray();
      }
    }
    if (body != null) {
      request.addHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(body.length));
    }
    // add headers
    request.getHeaders().forEach(
        (k, v) -> bytes.write(k).write(Strings.COLON).write(Bytes.SPACE).write(v).write(Bytes.CR)
            .write(Bytes.LF));
    bytes.write(Bytes.CR).write(Bytes.LF);
    context.write(bytes.toArray());

    if (body != null) {
      context.write(body);
    }
    return getResult(future, request);
  }


  private ChannelContext getChannel(URI uri) throws IOException {
    return CLIENT.connect(uri.getHost(), uri.getPort());
  }

  private void waitChannelConnected(HttpRequest request, ChannelContext context) {
    if (request.getConnectTimeout() > 0) {
      try {
        context.join(request.getConnectTimeout(), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw Exceptions.wrap(e);
      } catch (ExecutionException | TimeoutException e) {
        throw Exceptions.wrap(e);
      }
    } else {
      context.join();
    }
  }

  private HttpResponse getResult(CompletableFuture<AioHttpResponse> future, HttpRequest request) {
    if (request.getReadTimeout() > 0) {
      try {
        return future.get(request.getReadTimeout(), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw Exceptions.wrap(e);
      } catch (ExecutionException | TimeoutException e) {
        throw Exceptions.wrap(e);
      }
    }
    return future.join();
  }

  @Slf4j
  private static class AioHandlerListener implements AioHandler, AioListener {

    @Override
    public ByteBuffer encode(Object data, ChannelContext channelContext) {
      return ByteBuffer.wrap((byte[]) data);
    }

    @Override
    public Object decode(ByteBuffer buffer, int readableSize, ChannelContext channelContext) {
      int index = buffer.position();
      ResponseStatusLine statusLine = HttpHeaders.parseResponseStatusLine(buffer);
      if (statusLine == null) {
        return null;
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
        log.debug("Received Http [{}] [{}]", channelContext.getId(),
            new String(buffer.array(), index, buffer.position(), StandardCharsets.UTF_8));
      }

      ByteArrayInputStream out = new ByteArrayInputStream(body);
      Charset charset = StandardCharsets.UTF_8;
      String contentType = HttpHeaders.getHeader(headers, HttpHeaders.CONTENT_TYPE);
      String split = "charset=";
      if (contentType != null && contentType.contains(split)) {
        charset = Charset.forName(contentType.split(split)[1]);
      }
      return new AioHttpResponse(Integer.parseInt(statusLine.getStatusText()),
          statusLine.getMessage(), out, charset, channelContext);
    }

    @Override
    public void onConnected(ChannelContext channelContext) {
      if (log.isDebugEnabled()) {
        log.debug("httpclient connected {}", channelContext);
      }
    }

    @Override
    public void onClosed(ChannelContext channelContext) {
      @SuppressWarnings("unchecked") CompletableFuture<AioHttpResponse> future = (CompletableFuture<AioHttpResponse>) channelContext.getAttr(
          AioHttpRequestExecution.class.getName());
      if (log.isDebugEnabled()) {
        log.debug("httpclient closed {}", channelContext);
      }
      if (future != null && !future.isDone()) {
        future.completeExceptionally(new ClosedChannelException());
      }
    }

    @Override
    public void handle(Object data, ChannelContext channelContext) {
      @SuppressWarnings("unchecked") CompletableFuture<AioHttpResponse> future = (CompletableFuture<AioHttpResponse>) channelContext.getAttr(
          AioHttpRequestExecution.class.getName());
      if (log.isDebugEnabled()) {
        log.debug("handle future {} {}", channelContext.getId(), future);
      }
      if (future != null) {
        future.complete((AioHttpResponse) data);
      }
    }
  }

  private static class AioHttpResponse extends HttpResponse {

    private final ChannelContext channel;

    public AioHttpResponse(int code, String message, InputStream body, Charset charset,
        ChannelContext channel) {
      super(code, message, body, charset);
      this.channel = channel;
    }

    @Override
    public void close() throws IOException {
      try {
        super.close();
      } finally {
        channel.close();
      }
    }
  }


}
