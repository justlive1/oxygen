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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * bean序列化
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class BeanSerializer implements Serializer {

  private final List<Serializer> serializers;

  @Override
  public int order() {
    return 50;
  }

  @Override
  public boolean supported(Class<?> type, Object value) {
    return value != null && !ClassUtils.isJavaInternalType(type);
  }

  @Override
  public void serialize(Object value, StringBuilder buf) {
    Class<?> type = value.getClass();
    try {
      PropertyDescriptor[] props = Introspector.getBeanInfo(type).getPropertyDescriptors();
      buf.append('{');
      for (PropertyDescriptor prop : props) {
        if (processProperty(value, prop, buf)) {
          buf.append(',');
        }
      }
      buf.deleteCharAt(buf.length() - 1).append('}');
    } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
      throw Exceptions.wrap(e);
    }
  }

  private boolean processProperty(Object bean, PropertyDescriptor prop, StringBuilder buf)
      throws InvocationTargetException, IllegalAccessException {
    Method method = prop.getReadMethod();
    if (method == null || method.getParameterCount() > 0) {
      return false;
    }
    Object value = method.invoke(bean);
    Serializer serializer = Serializer.lookup(serializers, value);
    if (serializer == null) {
      return false;
    }
    buf.append('"').append(prop.getName()).append("\":");
    serializer.serialize(value, buf);
    return true;
  }


}
