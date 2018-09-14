package vip.justlive.oxygen.core.util;

import java.util.WeakHashMap;
import java.util.regex.Pattern;

/**
 * 路径匹配
 *
 * @author wubo
 */
public class PathMatcher {

  static final char ANY = '*';
  static final String ANY_REGEX = ".*";
  static final char ONLY_ONE = '?';
  static final String ONLY_NOE_REGEX = ".";
  static final String NOT_SEPARATOR_REGEX = "[^/]*";

  final WeakHashMap<String, Pattern> patterns = new WeakHashMap<>(32);

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
      Pattern p = patterns.get(pattern);
      if (p == null) {
        p = parsePattern(pattern);
        patterns.put(pattern, p);
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
    for (int i = 0; i < len; i++) {
      if (chars[i] == ANY) {
        if (pre) {
          // 第二次遇到*，替换成.*
          sb.append(ANY_REGEX);
        } else if (i + 1 == len) {
          // 单星是最后一个字符，则直接将*转成[^/]*
          sb.append(NOT_SEPARATOR_REGEX);
        } else {
          pre = true;
        }
      } else {
        if (pre) {
          sb.append(NOT_SEPARATOR_REGEX);
          pre = false;
        }
        // 遇到？替换成. 否则不变
        sb.append(chars[i] == ONLY_ONE ? ONLY_NOE_REGEX : chars[i]);
      }
    }
    return Pattern.compile(sb.toString());
  }

}
