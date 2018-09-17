package vip.justlive.oxygen.core.ioc;


import vip.justlive.oxygen.core.annotation.Bean;
import vip.justlive.oxygen.core.annotation.Configuration;

@Configuration
public class Conf {

  @Bean
  Inter noDepBean() {
    return new NoDepBean();
  }

}
