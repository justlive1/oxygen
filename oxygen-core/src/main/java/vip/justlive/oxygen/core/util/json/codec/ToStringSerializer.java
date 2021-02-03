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

package vip.justlive.oxygen.core.util.json.codec;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import vip.justlive.oxygen.core.util.base.ClassUtils;

/**
 * ToString 序列化,优先级较低
 *
 * @author wubo
 */
public class ToStringSerializer implements Serializer {

  private static final List<Class<?>> NON_WRAPPED = new ArrayList<>();
  private static final List<Class<?>> WRAPPED = new ArrayList<>();

  static {
    addNonWrappedType(Number.class);
    ClassUtils.allPrimitiveTypes().forEach(ToStringSerializer::addNonWrappedType);
    ClassUtils.allWrapperTypes().forEach(ToStringSerializer::addNonWrappedType);
    Arrays.asList(Temporal.class, Charset.class, Pattern.class, Locale.class, URI.class, URL.class,
        UUID.class, Enum.class, Currency.class, ZoneId.class)
        .forEach(ToStringSerializer::addWrappedType);
  }

  /**
   * 添加不用添加""的类型
   *
   * @param type 类型
   */
  public static void addNonWrappedType(Class<?> type) {
    if (!NON_WRAPPED.contains(type)) {
      NON_WRAPPED.add(type);
    }
  }

  /**
   * 添加需要""的类型
   *
   * @param type 类型
   */
  public static void addWrappedType(Class<?> type) {
    if (!WRAPPED.contains(type)) {
      WRAPPED.add(type);
    }
  }

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean supported(Class<?> type, Object value) {
    return value != null && (isIn(type, NON_WRAPPED) || isIn(type, WRAPPED));
  }

  @Override
  public void serialize(Object value, StringBuilder buf) {
    boolean wrapper = isIn(value.getClass(), WRAPPED);
    if (wrapper) {
      buf.append('"');
    }
    buf.append(value.toString());
    if (wrapper) {
      buf.append('"');
    }
  }

  private boolean isIn(Class<?> type, List<Class<?>> types) {
    if (types.contains(type)) {
      return true;
    }
    for (Class<?> wrap : types) {
      if (wrap.isAssignableFrom(type)) {
        return true;
      }
    }
    return false;
  }
}
