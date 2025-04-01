module oxygen.web.undertow {

  requires org.slf4j;
  requires oxygen.core;
  requires oxygen.web;
  requires undertow.core;
  requires undertow.servlet;

  requires static lombok;

  opens vip.justlive.oxygen.web.undertow;

  provides vip.justlive.oxygen.web.server.WebServer with vip.justlive.oxygen.web.undertow.UndertowWebServer;

  exports vip.justlive.oxygen.web.undertow;
}