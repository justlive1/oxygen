package vip.justlive.oxygen.core.ioc;


import vip.justlive.oxygen.core.annotation.Bean;
import vip.justlive.oxygen.core.annotation.Inject;

@Bean("xxx")
public class DepBean implements Inter {

  private final NoDepBean noDepBean;

  @Inject
  public DepBean(NoDepBean noDepBean) {
    this.noDepBean = noDepBean;
  }

  @Log
  @Override
  public void print() {
    System.out.println("this bean has dependency of NoDepBean");
    noDepBean.print();
    throw new IllegalArgumentException();
  }

}
