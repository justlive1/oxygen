package vip.justlive.oxygen.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 各种类型的输入公共接口
 *
 * @author wubo
 */
public interface SourceStream {

  /**
   * 获取输入流
   *
   * @return 输入流
   * @throws IOException io异常
   */
  InputStream getInputStream() throws IOException;
}
