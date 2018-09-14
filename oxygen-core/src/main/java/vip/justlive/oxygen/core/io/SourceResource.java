package vip.justlive.oxygen.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * 各种类型的资源公共接口，包括文件系统的文件或者是classpath下的资源
 *
 * @author wubo
 */
public interface SourceResource extends SourceStream {

  /**
   * 返回资源路径
   *
   * @return 资源路径
   */
  String path();

  /**
   * 是否存在
   *
   * @return 是否存在
   */
  default boolean exist() {
    try {
      return getFile().exists();
    } catch (IOException e) {
      try {
        InputStream in = getInputStream();
        in.close();
        return true;
      } catch (IOException e1) {
        return false;
      }
    }
  }

  /**
   * 是否文件
   *
   * @return 是否是文件
   */
  boolean isFile();

  /**
   * 获取文件
   *
   * @return 文件
   * @throws IOException io异常
   */
  File getFile() throws IOException;

  /**
   * 获取资源的URL
   *
   * @return URL
   * @throws IOException io异常
   */
  URL getURL() throws IOException;

  /**
   * 使用path创建相对路径的{@code SourceResource}
   *
   * @param path 路径
   * @return 源资源
   * @throws IOException io异常
   */
  SourceResource createRelative(String path) throws IOException;
}
