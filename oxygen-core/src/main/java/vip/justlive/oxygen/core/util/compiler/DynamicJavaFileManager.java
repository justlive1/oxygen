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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * 动态编译java文件管理
 *
 * @author wubo
 */
public class DynamicJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

  private final ClassLoader loader;
  private final Map<String, SourceCode> sources;
  private final Map<String, ByteCode> byteCodes;

  public DynamicJavaFileManager(JavaFileManager fileManager, ClassLoader loader,
      Map<String, SourceCode> sources, Map<String, ByteCode> byteCodes) {
    super(fileManager);
    this.loader = loader;
    this.sources = sources;
    this.byteCodes = byteCodes;
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
      FileObject sibling) throws IOException {
    if (kind == Kind.CLASS && location == StandardLocation.CLASS_OUTPUT) {
      return byteCodes.computeIfAbsent(className, k -> new ByteCode(k, kind));
    }
    return super.getJavaFileForOutput(location, className, kind, sibling);
  }

  @Override
  public ClassLoader getClassLoader(Location location) {
    if (loader != null) {
      return loader;
    }
    return super.getClassLoader(location);
  }

  @Override
  public String inferBinaryName(Location location, JavaFileObject file) {
    if (file instanceof ByteCode || file instanceof SourceCode) {
      return file.getName();
    }
    return super.inferBinaryName(location, file);
  }

  @Override
  public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds,
      boolean recurse) throws IOException {
    List<JavaFileObject> list = new ArrayList<>();
    super.list(location, packageName, kinds, recurse).forEach(list::add);

    if (location == StandardLocation.CLASS_PATH && kinds.contains(Kind.CLASS)) {
      byteCodes.forEach((k, v) -> {
        if (contains(k, packageName, recurse)) {
          list.add(v);
        }
      });
    } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(Kind.SOURCE)) {
      sources.forEach((k, v) -> {
        if (contains(k, packageName, recurse)) {
          list.add(v);
        }
      });
    }
    return list;
  }

  private boolean contains(String name, String packageName, boolean recurse) {
    String pkgName = name.substring(0, name.lastIndexOf(Strings.DOT));
    if (recurse) {
      return pkgName.startsWith(packageName);
    } else {
      return pkgName.equals(packageName);
    }
  }
}
