package vip.justlive.oxygen.core.util;

import java.net.InetAddress;
import java.util.concurrent.ThreadLocalRandom;
import javax.net.ServerSocketFactory;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.exception.Exceptions;

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
   * 默认最小端口
   */
  public static final int DEFAULT_PORT_RANGE_MIN = 10000;
  /**
   * 最大端口
   */
  public static final int PORT_RANGE_MAX = 65535;

  /**
   * 是否为有效的端口号，不检验占用
   *
   * @param port 端口号
   * @return true为有效
   */
  public static boolean isValidPort(int port) {
    return port > 0 && port <= PORT_RANGE_MAX;
  }

  /**
   * 端口是否可用
   *
   * @param port 端口
   * @return true为可用
   */
  public static boolean isAvailablePort(int port) {
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

  /**
   * 查询可用的端口号 [10000, 65535]
   *
   * @return port
   */
  public static int findAvailablePort() {
    return findAvailablePort(DEFAULT_PORT_RANGE_MIN);
  }

  /**
   * 查询可用的端口号 [minPort, 65535]
   *
   * @param minPort 最小端口
   * @return port
   */
  public static int findAvailablePort(int minPort) {
    return findAvailablePort(minPort, PORT_RANGE_MAX);
  }

  /**
   * 查询可用的端口号 [minPort, maxPort]
   *
   * @param minPort 最小端口
   * @param maxPort 最大端口
   * @return port
   */
  public static int findAvailablePort(int minPort, int maxPort) {
    if (minPort < 0) {
      throw Exceptions.fail("'minPort' must be greater than 0");
    }
    if (minPort > maxPort) {
      throw Exceptions.fail("'maxPort' must be greater than or equal to 'minPort'");
    }
    if (maxPort > PORT_RANGE_MAX) {
      throw Exceptions.fail("'maxPort' must be less than or equal to " + PORT_RANGE_MAX);
    }
    int portRange = maxPort - minPort;
    int searchCounter = 0;
    int port;
    while (searchCounter < portRange) {
      port = ThreadLocalRandom.current().nextInt(portRange + 1);
      if (isAvailablePort(port)) {
        return port;
      }
      searchCounter++;
    }

    throw Exceptions.fail(String
        .format("Can not find available port in range [%d, %d] after [%d] attempts", minPort,
            maxPort, searchCounter));
  }
}
