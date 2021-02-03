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

import java.util.WeakHashMap;
import java.util.regex.Pattern;

/**
 * 分隔符匹配器
 *
 * @author wubo
 */
public class SplitterMatcher {

  private static final String ANY_REGEX = ".*";
  private static final String DB_ANY_REGEX_TPL = "[%s]?";
  private static final String NOT_SEPARATOR_REGEX_TPL = "[^%s]*";
  private static final WeakHashMap<String, Pattern> PATTERNS = new WeakHashMap<>(32);

  private final char splitter;
  private final String dbAnyRegex;
  private final String notSeparatorRegex;

  public SplitterMatcher(char splitter) {
    this.splitter = splitter;
    this.dbAnyRegex = String.format(DB_ANY_REGEX_TPL, splitter);
    this.notSeparatorRegex = String.format(NOT_SEPARATOR_REGEX_TPL, splitter);
  }


  /**
   * 匹配路径
   *
   * @param pattern 匹配串
   * @param path 路径
   * @return true 匹配上了
   */
  public boolean match(String pattern, String path) {
    boolean fastFail =
        path == null || (path.length() > 0 && (path.charAt(0) == splitter) != (pattern.charAt(0)
            == splitter));
    if (fastFail) {
      return false;
    }
    if (Strings.isPattern(pattern)) {
      return PATTERNS.computeIfAbsent(pattern, this::parsePattern).matcher(path).matches();
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
    if (chars[i] == Bytes.ANY) {
      if (pre) {
        // 第二次遇到*，替换成.*
        sb.append(ANY_REGEX);
        dbPre = true;
      } else if (i + 1 == chars.length) {
        // 单星是最后一个字符，则直接将*转成[^/]*
        sb.append(notSeparatorRegex);
      } else {
        pre = true;
      }
    } else {
      if (dbPre && chars[i] == splitter) {
        sb.append(dbAnyRegex);
      } else if (!dbPre && pre) {
        sb.append(notSeparatorRegex);
      }
      if (chars[i] == Bytes.QUESTION_MARK) {
        // 遇到？替换成. 否则不变
        sb.append(Strings.DOT);
      } else if (!dbPre || chars[i] != splitter) {
        sb.append(chars[i]);
      }
      pre = false;
      dbPre = false;
    }
    return new boolean[]{pre, dbPre};
  }

}
