module oxygen.web.jetty {

  requires org.slf4j;
  requires org.eclipse.jetty.ee10.apache.jsp;
  requires org.eclipse.jetty.ee10.webapp;
  requires org.eclipse.jetty.server;

  requires oxygen.core;
  requires oxygen.web;

  requires static lombok;

  opens vip.justlive.oxygen.web.jetty;

  exports vip.justlive.oxygen.web.jetty;

  provides vip.justlive.oxygen.web.server.WebServer with vip.justlive.oxygen.web.jetty.JettyWebServer;
}