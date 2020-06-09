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

import java.net.URI;
import javax.tools.SimpleJavaFileObject;

/**
 * java源码
 *
 * @author wubo
 */
public class SourceCode extends SimpleJavaFileObject {

  private final String name;
  private final CharSequence source;

  protected SourceCode(String name, CharSequence source) {
    super(URI.create("mem:///src/" + name + Kind.SOURCE.extension), Kind.SOURCE);
    this.name = name;
    this.source = source;
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return source;
  }

  @Override
  public String getName() {
    return name;
  }
}
