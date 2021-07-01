/*
 * Copyright (C) 2021 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 关键字搜索
 *
 * @author wubo
 */
public class WordSearch {

  private final TrieNode root = new TrieNode();

  /**
   * 添加关键字
   *
   * @param keyword 关键字
   */
  public void addKeyword(String keyword) {
    if (keyword == null || keyword.isEmpty()) {
      return;
    }

    TrieNode node = root;

    for (Character c : keyword.toCharArray()) {
      node = node.nodes.computeIfAbsent(c, k -> new TrieNode());
    }

    node.end = true;
  }

  /**
   * 查找第一个关键字
   *
   * @param text 文本
   * @return 结果
   */
  public Result findFirst(String text) {
    return findFirst(text, false);
  }

  /**
   * 查找第一个关键字
   *
   * @param text         文本
   * @param ignoreSymbol 是否忽略符号
   * @return 结果
   */
  public Result findFirst(String text, boolean ignoreSymbol) {
    if (text == null || text.isEmpty()) {
      return null;
    }

    TrieNode node = root;

    int offset = 0;
    char[] chars = text.toCharArray();
    for (int index = 0; index < chars.length; index++) {
      char c = chars[index];
      // 特殊符号
      if (ignoreSymbol && isSymbol(c)) {
        if (node == root) {
          offset++;
        }
        continue;
      }
      node = node.next(c);
      if (node == null) {
        // 不是关键字
        node = root;
        offset = index;
      } else if (node.end) {
        // 发现关键字
        return new Result(offset + 1, text.substring(offset + 1, index + 1));
      }
    }
    return null;
  }

  /**
   * 找出所有关键字
   *
   * @param text 文本
   * @return 结果
   */
  public List<Result> findAll(String text) {
    return findAll(text, false);
  }

  /**
   * 找出所有关键字
   *
   * @param text         文本
   * @param ignoreSymbol 是否忽略符号
   * @return 结果
   */
  public List<Result> findAll(String text, boolean ignoreSymbol) {
    List<Result> list = new ArrayList<>();
    if (text == null || text.isEmpty()) {
      return list;
    }

    TrieNode node = root;

    int offset = 0;
    char[] chars = text.toCharArray();
    for (int index = 0; index < chars.length; index++) {
      char c = chars[index];
      // 特殊符号
      if (ignoreSymbol && isSymbol(c)) {
        if (node == root) {
          offset++;
        }
        continue;
      }
      node = node.next(c);
      if (node == null) {
        // 不是关键字
        node = root;
        offset = index;
      } else if (node.end) {
        // 发现关键字
        list.add(new Result(offset + 1, text.substring(offset + 1, index + 1)));
      }
    }
    return list;
  }

  /**
   * 替换关键字
   *
   * @param text       文本
   * @param replaceStr 替换的字符
   * @return 结果
   */
  public String replace(String text, String replaceStr) {
    return replace(text, replaceStr, false);
  }

  /**
   * 替换关键字
   *
   * @param text         文本
   * @param replaceStr   替换的字符
   * @param ignoreSymbol 是否忽略符号
   * @return 结果
   */
  public String replace(String text, String replaceStr, boolean ignoreSymbol) {
    if (text == null || text.isEmpty()) {
      return text;
    }

    TrieNode node = root;
    StringBuilder sb = new StringBuilder(text);

    List<int[]> replaces = new ArrayList<>();

    int offset = 0;
    char[] chars = text.toCharArray();
    for (int index = 0; index < chars.length; index++) {
      char c = chars[index];
      // 特殊符号
      if (ignoreSymbol && isSymbol(c)) {
        if (node == root) {
          offset++;
        }
        continue;
      }

      node = node.next(c);
      if (node == null) {
        // 说明不是关键字
        node = root;
        offset = index;
      } else if (node.end) {
        // 发现关键字
        replaces.add(new int[]{offset + 1, index + 1});
        node = root;
        offset = index;
      }
    }

    // 处理关键字
    for (int i = replaces.size() - 1; i >= 0; i--) {
      int[] rep = replaces.get(i);
      sb.replace(rep[0], rep[1], replaceStr);
    }
    return sb.toString();
  }

  private boolean isSymbol(Character ch) {
    // 0x2E80~0x9FFF 是东亚文字范围
    return (ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z') && (ch < '0' || ch > '9') && (
        ch < 0x2E80 || ch > 0x9FFF);
  }

  @Data
  public static class Result {

    private final int index;
    private final String keyword;
  }

  private static class TrieNode {

    boolean end;
    final Map<Character, TrieNode> nodes = new HashMap<>();

    TrieNode next(Character c) {
      return nodes.get(c);
    }
  }
}
