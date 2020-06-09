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
package vip.justlive.oxygen.core.cache;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vip.justlive.oxygen.core.aop.Invocation;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * 默认缓存key生成器
 *
 * @author wubo
 */
public class KeyGeneratorImpl implements KeyGenerator {

  private static final Pattern ARGS_PATTERN = Pattern.compile("args\\[(\\d+)[]]");
  private static final Pattern FIELD_PATTERN = Pattern.compile("\\[([^\\[\\]]+)[]]");

  @Override
  public String generate(Cacheable cacheable, Invocation invocation) {
    Object[] params = invocation.getArgs();
    Method method = invocation.getMethod();
    if (!Strings.hasText(cacheable.key())) {
      String src;
      if (params.length > 0) {
        src = String.join(Strings.DOT, method.getDeclaringClass().getName(), method.getName(),
            Arrays.deepToString(params));
      } else {
        src = String.join(Strings.DOT, method.getDeclaringClass().getName(), method.getName());
      }
      return src;
    }
    return String.join(Strings.DOT, method.getDeclaringClass().getName(), method.getName(),
        MoreObjects.safeToString(parse(cacheable.key(), params)));
  }

  private Object parse(String key, Object[] args) {
    Matcher matcher = ARGS_PATTERN.matcher(key);
    if (!matcher.find()) {
      throw Exceptions.fail(String.format("cache key [%s] is illegal", key));
    }
    String source = matcher.group(0);
    int index = Integer.parseInt(matcher.group(1));
    if (index > args.length - 1) {
      throw Exceptions.fail(String.format("cache key [%s] is illegal, out of bounds", key));
    }

    matcher = FIELD_PATTERN.matcher(key.substring(source.length()));
    index = 0;
    String field;
    Object value = args[index];
    while (matcher.find(index)) {
      if (value == null) {
        return null;
      }
      field = matcher.group(1);
      value = ClassUtils.getValue(value, field);
      index = matcher.end();
    }
    return value;
  }
}
