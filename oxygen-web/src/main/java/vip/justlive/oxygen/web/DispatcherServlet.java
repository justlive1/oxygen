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

import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.web.http.Cookie;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.RequestParse;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.mapping.Action;
import vip.justlive.oxygen.web.mapping.Mapping.HttpMethod;
import vip.justlive.oxygen.web.mapping.StaticMapping.StaticException;
import vip.justlive.oxygen.web.mapping.StaticMapping.StaticSource;
import vip.justlive.oxygen.web.view.ViewResolver;

/**
 * 请求分发器
 *
 * @author wubo
 */
@Slf4j
public class DispatcherServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private void doService(HttpServletRequest req, HttpServletResponse resp, HttpMethod httpMethod) {
    String requestPath = req.getServletPath();
    if (log.isDebugEnabled()) {
      log.debug("DispatcherServlet accept request for [{}] on method [{}]", requestPath,
          httpMethod);
    }

    Request.set(req);
    Response.set(resp);

    try {
      Action action = WebPlugin.findActionByPath(requestPath, httpMethod);
      if (action == null) {
        handlerNotFound(req, resp);
        return;
      }
      handlerAction(action, req, resp);
    } catch (StaticException e) {
      handlerStatic(req, resp, e.getSource());
    } finally {
      Request.clear();
      Response.clear();
    }

  }

  private void handlerAction(Action action, HttpServletRequest req, HttpServletResponse resp) {
    Request.current().setAction(action);
    for (RequestParse requestParse : WebPlugin.REQUEST_PARSES) {
      if (requestParse.supported(req)) {
        requestParse.handle(req);
      }
    }

    try {
      Object result = action.invoke();
      copyResponse(Response.current(), resp);
      if (action.needRenderView()) {
        ViewResolver viewResolver = WebPlugin.findViewResolver(result);
        if (viewResolver == null) {
          handlerNoViewResolver(resp);
          return;
        }
        viewResolver.resolveView(req, resp, result);
      }
    } catch (Exception e) {
      handlerError(req, resp, e);
    }

  }

  private void copyResponse(Response response, HttpServletResponse resp) {
    if (response.getContentType() != null) {
      resp.setContentType(response.getContentType());
    }
    resp.setCharacterEncoding(response.getEncoding());
    response.getHeaders().forEach(resp::addHeader);
    for (Cookie cookie : response.getCookies().values()) {
      javax.servlet.http.Cookie jCookie = new javax.servlet.http.Cookie(cookie.getName(),
          cookie.getValue());
      jCookie.setPath(cookie.getPath());
      jCookie.setSecure(cookie.isSecure());
      if (cookie.getMaxAge() != null) {
        jCookie.setMaxAge(cookie.getMaxAge());
      }
      if (cookie.getDomain() != null) {
        jCookie.setDomain(cookie.getDomain());
      }
      resp.addCookie(jCookie);
    }

  }

  private void handlerNoViewResolver(HttpServletResponse resp) {
    String msg = String
        .format("No ViewResolver found in container for [%s]", Request.current().getPath());
    log.error(msg);
    try {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private void handlerNotFound(HttpServletRequest req, HttpServletResponse resp) {
    if (log.isDebugEnabled()) {
      log.debug("DispatcherServlet not found path [{}] on method [{}]", req.getServletPath(),
          req.getMethod());
    }
    WebPlugin.ERROR_HANDLERS.get(Constants.NOT_FOUND).handle(req, resp);
  }

  private void handlerError(HttpServletRequest req, HttpServletResponse resp, Exception e) {
    log.error("DispatcherServlet occurs an error for path [{}]", req.getServletPath(), e);
    Request.current().setException(e);
    WebPlugin.ERROR_HANDLERS.get(Constants.SERVER_ERROR).handle(req, resp);
  }

  private void handlerStatic(HttpServletRequest req, HttpServletResponse resp,
      StaticSource source) {
    if (log.isDebugEnabled()) {
      log.debug("handle static source [{}] for path [{}]", source.getPath(), req.getServletPath());
    }
    resp.setContentType(source.getContentType());
    String browserETag = req.getHeader(Constants.IF_NONE_MATCH);
    String ifModifiedSince = req.getHeader(Constants.IF_MODIFIED_SINCE);
    long last = source.lastModified();
    String eTag = source.eTag();
    SimpleDateFormat format = new SimpleDateFormat(Constants.ETAG_DATA_FORMAT, Locale.US);
    resp.setHeader(Constants.ETAG, eTag);
    try {
      if (eTag.equals(browserETag) && ifModifiedSince != null
          && format.parse(ifModifiedSince).getTime() >= last) {
        resp.setStatus(304);
        return;
      }
    } catch (ParseException e) {
      log.warn("Can't parse 'If-Modified-Since' header date [{}]", ifModifiedSince);
    }
    String lastDate = format.format(new Date(last));
    resp.setHeader(Constants.LAST_MODIFIED, lastDate);
    resp.setHeader(Constants.CACHE_CONTROL,
        Constants.MAX_AGE + Constants.EQUAL + ConfigFactory.load(WebConf.class).getStaticCache());
    try {
      Files.copy(source.getPath(), resp.getOutputStream());
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }


  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (HttpMethod.PATCH.name().equals(req.getMethod())) {
      doService(req, resp, HttpMethod.PATCH);
    } else {
      super.service(req, resp);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.GET);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.POST);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.DELETE);
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.HEAD);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.PUT);
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.OPTIONS);
  }


  @Override
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.TRACE);
  }


}
