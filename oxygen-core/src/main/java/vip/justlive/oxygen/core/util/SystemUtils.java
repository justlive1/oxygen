package vip.justlive.oxygen.core.util;

import java.net.InetAddress;
import javax.net.ServerSocketFactory;
import lombok.experimental.UtilityClass;

/**
 * system utils
 *
 * @author wubo
 */
@UtilityClass
public class SystemUtils {

  /**
   * 本地地址
   */
  public final static String LOCAL_IP = "127.0.0.1";

  /**
   * 是否为有效的端口号，不检验占用
   *
   * @param port 端口号
   * @return true为有效
   */
  public static boolean isValidPort(int port) {
    return port > 0 && port <= 0xFFFF;
  }

  /**
   * 端口是否可用
   *
   * @param port 端口
   * @return true为可用
   */
  public static boolean isAvalibalePort(int port) {
    if (!isValidPort(port)) {
      return false;
    }
    try {
      ServerSocketFactory.getDefault().createServerSocket(port, 1, InetAddress.getByName(LOCAL_IP))
          .close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
