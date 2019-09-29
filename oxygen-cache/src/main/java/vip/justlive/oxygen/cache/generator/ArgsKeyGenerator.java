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
package vip.justlive.oxygen.cache.generator;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.MoreObjects;
import vip.justlive.oxygen.core.util.ReflectUtils;
import vip.justlive.oxygen.core.util.Strings;
import vip.justlive.oxygen.ioc.annotation.Bean;

/**
 * 缓存key生成器 使用入参
 *
 * @author wubo
 */
@Bean
public class ArgsKeyGenerator implements KeyGenerator {

  private static final Pattern ARGS_PATTERN = Pattern.compile("args\\[(\\d+)[]]");
  private static final Pattern FIELD_PATTERN = Pattern.compile("\\[([^\\[\\]]+)[]]");

  @Override
  public Object generate(Object target, Method method, Object... params) {
    if (params.length != 2) {
      throw new IllegalArgumentException();
    }
    String key = params[0].toString();
    Object[] args = (Object[]) params[1];
    return String.join(Strings.DOT, method.getDeclaringClass().getName(), method.getName(),
        MoreObjects.safeToString(parse(key, args)));
  }

  private Object parse(String key, Object[] args) {
    Matcher matcher = ARGS_PATTERN.matcher(key);
    if (matcher.find()) {
      String source = matcher.group(0);
      int index = Integer.parseInt(matcher.group(1));
      if (index > args.length - 1) {
        throw Exceptions.fail(String.format("Cacheable.key[%s] illegal, out of bounds", key));
      }
      return parse(key.substring(source.length()), args[index]);
    } else {
      throw Exceptions.fail(String.format("Cacheable.key[%s] illegal", key));
    }
  }

  private Object parse(String key, Object obj) {
    Matcher matcher = FIELD_PATTERN.matcher(key);
    int index = 0;
    String field;
    Object value = obj;
    while (matcher.find(index)) {
      if (value == null) {
        return null;
      }
      field = matcher.group(1);
      value = ReflectUtils.getValue(value, field);
      index = matcher.end();
    }
    return value;
  }

}
