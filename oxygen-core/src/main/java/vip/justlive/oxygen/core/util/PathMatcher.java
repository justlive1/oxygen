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

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * 路径匹配
 *
 * @author wubo
 */
@Slf4j
public class PathMatcher {

  private static final char ANY = '*';
  private static final String ANY_REGEX = ".*";
  private static final String DB_ANY_REGEX = "[/]?";
  private static final char ONLY_ONE = '?';
  private static final String ONLY_NOE_REGEX = ".";
  private static final char SLASH = '/';
  private static final String NOT_SEPARATOR_REGEX = "[^/]*";
  private static final WeakHashMap<String, Pattern> PATTERNS = new WeakHashMap<>(32);

  /**
   * 是否是通配符
   *
   * @param path 路径
   * @return true 是通配符
   */
  public boolean isPattern(String path) {
    return (path.indexOf(ANY) != -1 || path.indexOf(ONLY_ONE) != -1);
  }

  /**
   * 匹配路径
   *
   * @param pattern 匹配串
   * @param path 路径
   * @return true 匹配上了
   */
  public boolean match(String pattern, String path) {
    if (this.isPattern(pattern)) {
      Pattern p = PATTERNS.get(pattern);
      if (p == null) {
        p = parsePattern(pattern);
        PATTERNS.put(pattern, p);
        return p.matcher(path).matches();
      }
      return p.matcher(path).matches();
    }
    return pattern.equals(path);
  }

  /**
   * 将通配符表达式转化为正则表达式
   *
   * @param pattern 匹配串
   * @return 正则
   */
  private Pattern parsePattern(String pattern) {
    char[] chars = pattern.toCharArray();
    int len = chars.length;
    StringBuilder sb = new StringBuilder();
    boolean pre = false;
    boolean dbPre = false;
    for (int i = 0; i < len; i++) {
      boolean[] dbp = parse(pre, dbPre, chars, i, sb);
      pre = dbp[0];
      dbPre = dbp[1];
    }
    return Pattern.compile(sb.toString());
  }

  private boolean[] parse(boolean pre, boolean dbPre, char[] chars, int i, StringBuilder sb) {
    if (chars[i] == ANY) {
      if (pre) {
        // 第二次遇到*，替换成.*
        sb.append(ANY_REGEX);
        dbPre = true;
      } else if (i + 1 == chars.length) {
        // 单星是最后一个字符，则直接将*转成[^/]*
        sb.append(NOT_SEPARATOR_REGEX);
      } else {
        pre = true;
      }
    } else {
      if (dbPre && chars[i] == SLASH) {
        sb.append(DB_ANY_REGEX);
      } else if (!dbPre && pre) {
        sb.append(NOT_SEPARATOR_REGEX);
      }
      if (chars[i] == ONLY_ONE) {
        // 遇到？替换成. 否则不变
        sb.append(ONLY_NOE_REGEX);
      } else if (!dbPre || chars[i] != SLASH) {
        sb.append(chars[i]);
      }
      pre = false;
      dbPre = false;
    }
    return new boolean[]{pre, dbPre};
  }

  /**
   * 获取不含通配符的根路径
   *
   * @param location 路径
   * @return 根路径
   */
  public String getRootDir(String location) {
    int prefixEnd = location.indexOf(Constants.COLON) + 1;
    int rootDirEnd = location.length();
    while (rootDirEnd > prefixEnd && isPattern(location.substring(prefixEnd, rootDirEnd))) {
      rootDirEnd = location.lastIndexOf(Constants.PATH_SEPARATOR, rootDirEnd - 2) + 1;
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
    String fullPattern = rootDir.getAbsolutePath()
        .replace(File.separator, Constants.PATH_SEPARATOR);
    if (!subPattern.startsWith(Constants.PATH_SEPARATOR)) {
      fullPattern += Constants.PATH_SEPARATOR;
    }
    fullPattern += subPattern.replace(File.separator, Constants.PATH_SEPARATOR);
    this.searchMatchedFiles(fullPattern, rootDir, files);
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
      String currentPath = content.getAbsolutePath()
          .replace(File.separator, Constants.PATH_SEPARATOR);
      if (content.isDirectory()) {
        if (!content.canRead() && log.isDebugEnabled()) {
          log.debug("dir [{}] has no read permission, skip");
        } else {
          this.searchMatchedFiles(fullPattern, content, files);
        }
      } else if (this.match(fullPattern, currentPath)) {
        files.add(content);
      }
    }
  }

}
