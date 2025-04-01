module oxygen.jdbc {

  requires java.sql;
  requires org.slf4j;

  requires oxygen.core;

  requires static com.zaxxer.hikari;
  requires static lombok;

  exports vip.justlive.oxygen.jdbc;
  exports vip.justlive.oxygen.jdbc.config;
  exports vip.justlive.oxygen.jdbc.handler;
  exports vip.justlive.oxygen.jdbc.interceptor;
  exports vip.justlive.oxygen.jdbc.job;
  exports vip.justlive.oxygen.jdbc.page;
  exports vip.justlive.oxygen.jdbc.record;

  opens vip.justlive.oxygen.jdbc.config;

  provides vip.justlive.oxygen.core.Plugin with vip.justlive.oxygen.jdbc.JdbcPlugin;

  provides vip.justlive.oxygen.jdbc.handler.ColumnHandler with
      vip.justlive.oxygen.jdbc.handler.FloatColumnHandler,
      vip.justlive.oxygen.jdbc.handler.IntegerColumnHandler,
      vip.justlive.oxygen.jdbc.handler.LongColumnHandler,
      vip.justlive.oxygen.jdbc.handler.ShortColumnHandler,
      vip.justlive.oxygen.jdbc.handler.DoubleColumnHandler,
      vip.justlive.oxygen.jdbc.handler.ByteColumnHandler,
      vip.justlive.oxygen.jdbc.handler.BooleanColumnHandler,
      vip.justlive.oxygen.jdbc.handler.StringColumnHandler,
      vip.justlive.oxygen.jdbc.handler.BigDecimalColumnHandler,
      vip.justlive.oxygen.jdbc.handler.DateColumnHandler;

  provides vip.justlive.oxygen.jdbc.handler.PropertyHandler with
      vip.justlive.oxygen.jdbc.handler.DatePropertyHandler,
      vip.justlive.oxygen.jdbc.handler.Java8DatePropertyHandler;

  provides vip.justlive.oxygen.jdbc.page.PageDialect with
      vip.justlive.oxygen.jdbc.page.MysqlPageDialect,
      vip.justlive.oxygen.jdbc.page.H2PageDialect,
      vip.justlive.oxygen.jdbc.page.OraclePageDialect,
      vip.justlive.oxygen.jdbc.page.DerbyPageDialect;

  uses vip.justlive.oxygen.jdbc.handler.ColumnHandler;
  uses vip.justlive.oxygen.jdbc.handler.PropertyHandler;
  uses vip.justlive.oxygen.jdbc.page.PageDialect;
}