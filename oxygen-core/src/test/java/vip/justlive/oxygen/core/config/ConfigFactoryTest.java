package vip.justlive.oxygen.core.config;

import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author wubo
 */
public class ConfigFactoryTest {

  @Data
  static class Prop {

    @Value("${fc.name}")
    private String name;

    @Value("${fc.age}")
    private Integer age;
  }

  @Test
  public void testLoadOneProp() {

    ConfigFactory.loadProperties("classpath:/config/config.properties");
    Prop prop = ConfigFactory.load(Prop.class);

    Assert.assertNotNull(prop);
    Assert.assertEquals("jack", prop.getName());
    Assert.assertEquals(new Integer(18), prop.getAge());

  }

  @Test
  public void testLoadOverride() {
    ConfigFactory.loadProperties("classpath:/config/config.properties", "classpath:/config/config2.properties");
    Prop prop = ConfigFactory.load(Prop.class);

    Assert.assertNotNull(prop);
    Assert.assertEquals(new Integer(19), prop.getAge());
  }

}