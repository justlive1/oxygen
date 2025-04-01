module oxygen.web {

  requires transitive jakarta.servlet;

  requires oxygen.core;
  requires org.slf4j;

  requires static fastjson;
  requires static freemarker;
  requires static lombok;
  requires static thymeleaf;

  opens vip.justlive.oxygen.web.bind;
  opens vip.justlive.oxygen.web.http;
  opens vip.justlive.oxygen.web.server.aio;

  exports vip.justlive.oxygen.web;
  exports vip.justlive.oxygen.web.annotation;
  exports vip.justlive.oxygen.web.bind;
  exports vip.justlive.oxygen.web.exception;
  exports vip.justlive.oxygen.web.hook;
  exports vip.justlive.oxygen.web.http;
  exports vip.justlive.oxygen.web.result;
  exports vip.justlive.oxygen.web.router;
  exports vip.justlive.oxygen.web.server;
  exports vip.justlive.oxygen.web.server.aio;
  exports vip.justlive.oxygen.web.servlet;

  provides vip.justlive.oxygen.core.Plugin with vip.justlive.oxygen.web.WebPlugin;
  provides jakarta.servlet.ServletContainerInitializer with vip.justlive.oxygen.web.servlet.ServletWebInitializer;

  uses jakarta.servlet.ServletContainerInitializer;
  uses vip.justlive.oxygen.web.server.WebServer;
}