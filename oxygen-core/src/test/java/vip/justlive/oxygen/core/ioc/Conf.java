package vip.justlive.oxygen.core.ioc;


import vip.justlive.oxygen.core.annotation.After;
import vip.justlive.oxygen.core.annotation.Bean;
import vip.justlive.oxygen.core.annotation.Before;
import vip.justlive.oxygen.core.annotation.Catching;
import vip.justlive.oxygen.core.annotation.Configuration;
import vip.justlive.oxygen.core.aop.Invocation;

@Configuration
public class Conf {

  @Bean
  Inter noDepBean() {
    return new NoDepBean();
  }

  @Before(annotation = Log.class)
  public void log(Invocation invocation) {
    System.out.println("aop before log " + invocation.getMethod());
  }

  @Before(annotation = Log.class)
  public void log1(Invocation invocation) {
    System.out.println("aop before log1 " + invocation.getMethod());
  }

  @After(annotation = Log.class)
  public void log2(Invocation invocation) {
    System.out.println("aop after log2 " + invocation.getMethod());
  }

  @Catching(annotation = Log.class)
  public void log3(Invocation invocation) {
    System.out.println("aop catching log3 " + invocation.getMethod());
  }
}
