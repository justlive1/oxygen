package vip.justlive.oxygen.core.ioc;


import vip.justlive.oxygen.core.aop.After;
import vip.justlive.oxygen.core.aop.Before;
import vip.justlive.oxygen.core.aop.Catching;
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
  public boolean log1(Invocation invocation) {
    System.out.println("aop before log1 " + invocation.getMethod());
    return false;
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
