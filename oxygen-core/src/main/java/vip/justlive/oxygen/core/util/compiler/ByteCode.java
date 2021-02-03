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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;
import lombok.Getter;
import lombok.Setter;

/**
 * java字节码
 *
 * @author wubo
 */
public class ByteCode extends SimpleJavaFileObject {

  @Getter
  @Setter
  private boolean defined;
  private final String name;
  private final ByteArrayOutputStream bytecode = new ByteArrayOutputStream();

  public ByteCode(String name, Kind kind) {
    super(URI.create("mem:///target/" + name + kind), kind);
    this.name = name;
  }

  @Override
  public InputStream openInputStream() {
    return new ByteArrayInputStream(getByteCode());
  }

  @Override
  public OutputStream openOutputStream() {
    return bytecode;
  }

  @Override
  public String getName() {
    return name;
  }

  public byte[] getByteCode() {
    return bytecode.toByteArray();
  }
}
