package vip.justlive.oxygen.core.ioc;


import vip.justlive.oxygen.core.config.Value;

@Bean("xxx")
public class DepBean implements Inter {

  private final NoDepBean noDepBean;

  @Inject
  public DepBean(NoDepBean noDepBean) {
    this.noDepBean = noDepBean;
  }

  @Value("${val.a:xxx}")
  private String val;

  @Log
  @Override
  public void print() {
    System.out.println("this bean has dependency of NoDepBean and val is " + val);
    noDepBean.print();
    throw new IllegalArgumentException();
  }

}
