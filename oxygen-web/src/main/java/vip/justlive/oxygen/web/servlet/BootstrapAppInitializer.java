package vip.justlive.oxygen.web.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.util.Strings;

/**
 * bootstrap initializer
 *
 * @author wubo
 */
public class BootstrapAppInitializer implements WebAppInitializer {

  @Override
  public int order() {
    return -100;
  }

  @Override
  public void onStartup(ServletContext context) {
    Bootstrap.start();

    Dynamic dynamic = context
        .addServlet(DispatcherServlet.class.getSimpleName(), DispatcherServlet.class);
    dynamic.setLoadOnStartup(0);
    dynamic.addMapping(Strings.SLASH);
    dynamic.setAsyncSupported(true);
  }
}
