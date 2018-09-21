package vip.justlive.oxygen.core.ioc;

import vip.justlive.oxygen.core.config.Value;

public class NoDepBean implements Inter {

  @Value("${val.b}")
  private Integer val;

  @Override
  public void print() {
    System.out.println("this is a non dependencies bean and val is " + val);
  }

}
