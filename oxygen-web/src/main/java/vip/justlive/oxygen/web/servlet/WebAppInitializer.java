package vip.justlive.oxygen.web.servlet;

import javax.servlet.ServletContext;
import vip.justlive.oxygen.core.Order;

/**
 * web application 启动初始接口，只有在servlet3.0+容器下才有效
 *
 * @author wubo
 */
public interface WebAppInitializer extends Order {

  /**
   * 启动执行
   *
   * @param context servlet上下文
   */
  void onStartup(ServletContext context);

}
