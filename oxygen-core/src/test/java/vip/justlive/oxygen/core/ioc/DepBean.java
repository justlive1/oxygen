package vip.justlive.oxygen.core.ioc;


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
