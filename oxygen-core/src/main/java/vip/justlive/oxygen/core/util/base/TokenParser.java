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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;

/**
 * token 解析器
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class TokenParser {

  private static final char ESC = '\\';

  private final String openToken;
  private final String closeToken;
  private final UnaryOperator<String> textHandler;
  private final UnaryOperator<String> tokenHandler;

  public TokenParser(String openToken, String closeToken) {
    this(openToken, closeToken, UnaryOperator.identity(), UnaryOperator.identity());
  }

  public TokenParser(String openToken, String closeToken, UnaryOperator<String> tokenHandler) {
    this(openToken, closeToken, UnaryOperator.identity(), tokenHandler);
  }

  /**
   * 解析
   *
   * @param text 需要处理的问题
   * @return 处理后的文本
   */
  public String parse(String text) {
    return parse(text, tokenHandler);
  }

  /**
   * 解析，使用自定义token逻辑处理
   *
   * @param text 需要处理的问题
   * @param tokenHandler token处理逻辑
   * @return 处理后的文本
   */
  public String parse(String text, UnaryOperator<String> tokenHandler) {
    return parse(text, textHandler, tokenHandler);
  }

  /**
   * 解析，使用自定义逻辑处理
   *
   * @param text 需要处理的问题
   * @param textHandler 普通文本处理逻辑
   * @param tokenHandler token处理逻辑
   * @return 处理后的文本
   */
  public String parse(String text, UnaryOperator<String> textHandler,
      UnaryOperator<String> tokenHandler) {
    MoreObjects.notNull(textHandler, "textHandler cannot be null");
    MoreObjects.notNull(tokenHandler, "tokenHandler cannot be null");
    final List<Pair<String, Boolean>> statements = new ArrayList<>(2);
    parse0(text, statements);
    StringBuilder sb = new StringBuilder();
    for (Pair<String, Boolean> statement : statements) {
      if (statement.getValue() != null && statement.getValue()) {
        sb.append(tokenHandler.apply(statement.getKey()));
      } else {
        sb.append(textHandler.apply(statement.getKey()));
      }
    }
    return sb.toString();
  }

  private void text(String text, List<Pair<String, Boolean>> statements) {
    statements.add(new Pair<String, Boolean>().setKey(text).setValue(false));
  }

  private void token(String text, List<Pair<String, Boolean>> statements) {
    statements.add(new Pair<String, Boolean>().setKey(text).setValue(true));
  }

  private void parse0(String text, List<Pair<String, Boolean>> statements) {
    if (text == null || text.isEmpty()) {
      return;
    }
    AtomicInteger start = new AtomicInteger(text.indexOf(openToken));
    if (start.get() == -1) {
      text(text, statements);
      return;
    }

    char[] chars = text.toCharArray();
    AtomicInteger index = new AtomicInteger(0);
    while (start.get() > -1) {
      searchExp(start, index, chars, text, statements);
    }
    // 处理剩余字符
    if (index.get() < chars.length) {
      text(new String(chars, index.get(), chars.length - index.get()), statements);
    }
  }

  private void searchExp(AtomicInteger start, AtomicInteger index, char[] chars, String text,
      List<Pair<String, Boolean>> statements) {
    StringBuilder exp = new StringBuilder();
    if (start.get() > 0 && chars[start.get() - 1] == ESC) {
      // open token被转义 装入字符并去除转义
      text(new String(chars, index.get(), start.get() - index.get() - 1).concat(openToken),
          statements);
      index.set(start.get() + openToken.length());
    } else {
      // 装入open token前的字符
      text(new String(chars, index.get(), start.get() - index.get()), statements);
      index.set(start.get() + openToken.length());
      // 查找close token
      AtomicInteger end = new AtomicInteger(text.indexOf(closeToken, index.get()));
      searchCloseToken(end, index, chars, text, exp);
      if (end.get() > -1) {
        // token
        token(exp.toString(), statements);
      } else {
        text(new String(chars, start.get(), chars.length - start.get()), statements);
        index.set(chars.length);
      }
    }
    start.set(text.indexOf(openToken, index.get()));
  }

  private void searchCloseToken(AtomicInteger end, AtomicInteger index, char[] chars, String text,
      StringBuilder exp) {
    while (end.get() > -1) {
      if (end.get() > index.get() && chars[end.get() - 1] == ESC) {
        // close token 被转义
        exp.append(chars, index.get(), end.get() - index.get() - 1).append(closeToken);
        index.set(end.get() + closeToken.length());
        end.set(text.indexOf(closeToken, index.get()));
      } else {
        // 找到close token
        exp.append(chars, index.get(), end.get() - index.get());
        index.set(end.get() + closeToken.length());
        break;
      }
    }
  }
}
