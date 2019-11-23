/*
 * Copyright (C) 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package vip.justlive.oxygen.core.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
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
  public static final String LOCAL_IP = "127.0.0.1";

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
    try (ServerSocket ignored = ServerSocketFactory.getDefault()
        .createServerSocket(port, 1, InetAddress.getByName(LOCAL_IP))) {
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

  /**
   * 解析成InetSocketAddress，格式host:port
   *
   * @param address 需要解析的地址
   * @return address
   */
  public static InetSocketAddress parseAddress(String address) {
    if (address == null || address.trim().length() == 0) {
      return null;
    }
    String[] hostPort = address.trim().split(Strings.COLON);
    return new InetSocketAddress(hostPort[0].trim(), Integer.parseInt(hostPort[1].trim()));
  }


  /**
   * 获取本机地址
   *
   * @return address
   */
  public static InetAddress getLocalAddress() {
    InetAddress candidateAddress = null;
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      if (interfaces != null) {
        candidateAddress = getByNetworkInterfaces(interfaces);
      }
    } catch (SocketException e) {
      // ignore
    }

    if (candidateAddress == null) {
      try {
        candidateAddress = InetAddress.getLocalHost();
      } catch (UnknownHostException e) {
        // ignore
      }
    }
    return candidateAddress;
  }

  private InetAddress getByNetworkInterfaces(Enumeration<NetworkInterface> interfaces) {
    InetAddress candidateAddress = null;
    while (interfaces.hasMoreElements()) {
      NetworkInterface network = interfaces.nextElement();
      Enumeration<InetAddress> addresses = network.getInetAddresses();
      while (addresses.hasMoreElements()) {
        InetAddress address = addresses.nextElement();
        if (address.isSiteLocalAddress()) {
          return address;
        } else if (!address.isLoopbackAddress() || !address.isLinkLocalAddress()) {
          candidateAddress = address;
        }
      }
    }
    return candidateAddress;
  }
}