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
package vip.justlive.oxygen.core.util.base;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 路径匹配
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class PathMatcher {

  private final SplitterMatcher MATCHER = new SplitterMatcher('/');

  /**
   * 匹配路径
   *
   * @param pattern 匹配串
   * @param path 路径
   * @return true 匹配上了
   */
  public boolean match(String pattern, String path) {
    return MATCHER.match(pattern, path);
  }

  /**
   * 获取不含通配符的根路径
   *
   * @param location 路径
   * @return 根路径
   */
  public String getRootDir(String location) {
    int prefixEnd = location.indexOf(Strings.COLON) + 1;
    int rootDirEnd = location.length();
    while (rootDirEnd > prefixEnd && Strings.isPattern(location.substring(prefixEnd, rootDirEnd))) {
      rootDirEnd = location.lastIndexOf(Strings.SLASH, rootDirEnd - 2) + 1;
    }
    if (rootDirEnd == 0) {
      rootDirEnd = prefixEnd;
    }
    return location.substring(0, rootDirEnd);
  }

  /**
   * 获取目录下匹配的文件
   *
   * @param rootDir 根目录
   * @param subPattern 匹配串
   * @return 文件列表
   */
  public Set<File> findMatchedFiles(File rootDir, String subPattern) {
    Set<File> files = new LinkedHashSet<>();
    if (!rootDir.exists() || !rootDir.isDirectory() || !rootDir.canRead()) {
      if (log.isWarnEnabled()) {
        log.warn("dir [{}] cannot execute search operation", rootDir.getPath());
      }
      return files;
    }
    String fullPattern = rootDir.getAbsolutePath().replace(File.separator, Strings.SLASH);
    if (!subPattern.startsWith(Strings.SLASH)) {
      fullPattern += Strings.SLASH;
    }
    fullPattern += subPattern.replace(File.separator, Strings.SLASH);
    searchMatchedFiles(fullPattern, rootDir, files);
    return files;
  }

  /**
   * 递归查询匹配的文件
   *
   * @param fullPattern 全匹配串
   * @param dir 目录
   * @param files 文件集合
   */
  private void searchMatchedFiles(String fullPattern, File dir, Set<File> files) {
    if (log.isDebugEnabled()) {
      log.debug("search files under dir [{}]", dir);
    }
    File[] dirContents = dir.listFiles();
    if (dirContents == null) {
      return;
    }
    for (File content : dirContents) {
      String currentPath = content.getAbsolutePath().replace(File.separator, Strings.SLASH);
      if (content.isDirectory()) {
        if (!content.canRead() && log.isDebugEnabled()) {
          log.debug("dir [{}] has no read permission, skip", content);
        } else {
          searchMatchedFiles(fullPattern, content, files);
        }
      } else if (match(fullPattern, currentPath)) {
        files.add(content);
      }
    }
  }

}
