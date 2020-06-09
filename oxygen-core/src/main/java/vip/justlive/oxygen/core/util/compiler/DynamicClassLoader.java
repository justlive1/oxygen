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
package vip.justlive.oxygen.core.util.compiler;

import java.util.Map;
import vip.justlive.oxygen.core.util.base.ClassUtils;

/**
 * 动态编译类加载器
 *
 * @author wubo
 */
public class DynamicClassLoader extends ClassLoader {

  private final Map<String, ByteCode> byteCodes;

  public DynamicClassLoader(ClassLoader parent, Map<String, ByteCode> byteCodes) {
    super(parent);
    this.byteCodes = byteCodes;
  }

  @Override
  protected Class<?> findClass(String name) {
    ByteCode byteCode = byteCodes.get(name);
    if (byteCode != null && !byteCode.isDefined()) {
      byte[] bytes = byteCode.getByteCode();
      byteCode.setDefined(true);
      return defineClass(name, bytes, 0, bytes.length);
    }
    return ClassUtils.forName(name, getParent());
  }
}
