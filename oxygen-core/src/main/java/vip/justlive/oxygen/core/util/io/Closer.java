/*
 * Copyright (C) 2020 the original author or authors.
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

package vip.justlive.oxygen.core.util.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import lombok.Getter;

/**
 * 收集多个可关闭资源，在close时全部关闭
 *
 * @author wubo
 */
public class Closer implements Closeable {

  private final Deque<Closeable> stack = new ArrayDeque<>(4);
  @Getter
  private Throwable thrown;

  /**
   * 注册需要关闭的资源
   *
   * @param closeable 资源
   * @param <T>       泛型
   * @return 资源
   */
  public <T extends Closeable> T register(T closeable) {
    if (closeable != null) {
      stack.addFirst(closeable);
    }
    return closeable;
  }

  /**
   * 在catch中添加异常
   *
   * @param throwable 异常
   */
  public void thrown(Throwable throwable) {
    thrown = throwable;
  }

  @Override
  public void close() throws IOException {
    closeQuietly();
    if (thrown instanceof IOException) {
      throw (IOException) thrown;
    }
    if (thrown != null) {
      throw new IOException(thrown);
    }
  }

  /**
   * 关闭
   */
  public void closeQuietly() {
    while (!stack.isEmpty()) {
      try {
        stack.pop().close();
      } catch (Throwable e) {
        if (thrown == null) {
          thrown = e;
        } else {
          thrown.addSuppressed(e);
        }
      }
    }
  }
}
