module oxygen.core {

  requires java.base;
  requires java.compiler;
  requires java.desktop;
  requires java.prefs;
  requires java.scripting;
  requires java.sql;

  requires org.slf4j;

  requires static net.sf.cglib;
  requires static lombok;

  exports vip.justlive.oxygen.core;
  exports vip.justlive.oxygen.core.aop;
  exports vip.justlive.oxygen.core.aop.interceptor;
  exports vip.justlive.oxygen.core.aop.invoke;
  exports vip.justlive.oxygen.core.aop.proxy;
  exports vip.justlive.oxygen.core.bean;
  exports vip.justlive.oxygen.core.cache;
  exports vip.justlive.oxygen.core.config;
  exports vip.justlive.oxygen.core.convert;
  exports vip.justlive.oxygen.core.exception;
  exports vip.justlive.oxygen.core.job;
  exports vip.justlive.oxygen.core.util.base;
  exports vip.justlive.oxygen.core.util.compiler;
  exports vip.justlive.oxygen.core.util.concurrent;
  exports vip.justlive.oxygen.core.util.crypto;
  exports vip.justlive.oxygen.core.util.eventbus;
  exports vip.justlive.oxygen.core.util.io;
  exports vip.justlive.oxygen.core.util.json;
  exports vip.justlive.oxygen.core.util.json.codec;
  exports vip.justlive.oxygen.core.util.net.http;
  exports vip.justlive.oxygen.core.util.net.aio;
  exports vip.justlive.oxygen.core.util.retry;
  exports vip.justlive.oxygen.core.util.scan;
  exports vip.justlive.oxygen.core.util.template;
  exports vip.justlive.oxygen.core.util.timer;

  opens vip.justlive.oxygen.core.aop;

  provides vip.justlive.oxygen.core.Plugin with
      vip.justlive.oxygen.core.config.ConfigPlugin,
      vip.justlive.oxygen.core.util.scan.ClassScannerPlugin,
      vip.justlive.oxygen.core.util.base.ResourceBundle,
      vip.justlive.oxygen.core.bean.IocPlugin,
      vip.justlive.oxygen.core.job.JobPlugin,
      vip.justlive.oxygen.core.cache.CachePlugin,
      vip.justlive.oxygen.core.aop.AopPlugin,
      vip.justlive.oxygen.core.bean.LifeCyclePlugin,
      vip.justlive.oxygen.core.util.concurrent.ThreadPoolMonitor;

  provides vip.justlive.oxygen.core.util.json.codec.Serializer with
      vip.justlive.oxygen.core.util.json.codec.DateSerializer,
      vip.justlive.oxygen.core.util.json.codec.NullSerializer,
      vip.justlive.oxygen.core.util.json.codec.StringSerializer,
      vip.justlive.oxygen.core.util.json.codec.ToStringSerializer;

  uses vip.justlive.oxygen.core.Plugin;
  uses vip.justlive.oxygen.core.bean.BeanProxy;
  uses vip.justlive.oxygen.core.util.json.codec.Serializer;

}