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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 文件自动清理器
 *
 * @author wubo
 */
public class FileCleaner implements AutoCloseable {

  private final Deque<File> deque = new ArrayDeque<>(4);

  /**
   * 添加需要清理的文件
   *
   * @param file 文件
   */
  public void track(File file) {
    if (file != null) {
      deque.add(file);
    }
  }

  /**
   * 添加需要清理的文件路径
   *
   * @param path 文件路径
   */
  public void track(Path path) {
    if (path != null) {
      deque.add(path.toFile());
    }
  }

  @Override
  public void close() {
    while (!deque.isEmpty()) {
      FileUtils.delete(deque.pop());
    }
  }
}
