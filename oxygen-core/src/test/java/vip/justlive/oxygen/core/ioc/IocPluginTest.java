package vip.justlive.oxygen.core.ioc;

import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.Bootstrap;

/**
 * @author wubo
 */
public class IocPluginTest {

  @Test
  public void test() {
    Bootstrap.initSystemPlugin();
    Assert.assertNotNull(BeanStore.getBean(Inter.class));
  }
}