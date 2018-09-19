package vip.justlive.oxygen.core.ioc;

import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.Bootstrap;

/**
 * @author wubo
 */
public class IocPluginTest {

  @Test(expected = IllegalArgumentException.class)
  public void test() {
    Bootstrap.initSystemPlugin();
    Inter inter = BeanStore.getBean("xxx", Inter.class);
    Assert.assertNotNull(inter);
    inter.print();
  }
}