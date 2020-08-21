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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.CoreConfigKeys;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.exception.WrappedException;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * 动态编译
 *
 * @author wubo
 */
@Slf4j
public class DynamicCompiler implements Closeable {

  private static final Pattern PACKAGE_PATTERN = Pattern
      .compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9.]*);");
  private static final Pattern CLASS_PATTERN = Pattern
      .compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)[\\s<]");
  private static final ClasspathTransfer TRANSFER = new ClasspathTransfer();

  private final ClassLoader loader;
  private final List<String> options;
  private final JavaCompiler compiler;
  private final JavaFileManager manager;
  private final Map<String, ByteCode> byteCodes = new HashMap<>();
  private final Map<String, SourceCode> sources = new HashMap<>();
  private final Collector<JavaFileObject> collector = new Collector<>();

  public DynamicCompiler() {
    ClassLoader parent = Thread.currentThread().getContextClassLoader();
    this.loader = new DynamicClassLoader(parent, byteCodes);
    this.compiler = ToolProvider.getSystemJavaCompiler();
    if (this.compiler == null) {
      throw Exceptions.fail("no compiler found");
    }
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(collector, null, null);

    if (parent instanceof URLClassLoader && parent != ClassLoader.getSystemClassLoader()) {
      try {
        List<File> files = new ArrayList<>();
        for (URL url : ((URLClassLoader) parent).getURLs()) {
          files.add(new File(url.getFile()));
        }
        TRANSFER.transfer(files);
        fileManager.setLocation(StandardLocation.CLASS_PATH, files);
      } catch (IOException e) {
        throw new IllegalStateException(e.getMessage(), e);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("ClassLoader: {}, StandardJavaFileManager class-path: {}", parent,
          fileManager.getLocation(StandardLocation.CLASS_PATH));
    }
    this.manager = new DynamicJavaFileManager(fileManager, loader, sources, byteCodes);
    this.options = new ArrayList<>();
    String value = CoreConfigKeys.COMPILER_OPTIONS.getValue();
    if (Strings.hasText(value)) {
      for (String option : value.split(Strings.COMMA)) {
        options.add(option.trim());
      }
    }
  }

  @Override
  public void close() throws IOException {
    this.manager.close();
    this.sources.clear();
    this.byteCodes.clear();
  }

  /**
   * 源码编译
   *
   * @param code 源码
   * @return class
   */
  public Class<?> compile(String code) {
    code = code.trim();
    Matcher matcher = PACKAGE_PATTERN.matcher(code);
    String packageName = Strings.EMPTY;
    if (matcher.find()) {
      packageName = matcher.group(1);
    }
    matcher = CLASS_PATTERN.matcher(code);
    String className;
    if (matcher.find()) {
      className = matcher.group(1);
    } else {
      throw new IllegalArgumentException("No such class name in " + code);
    }
    return compile(packageName, className, code);
  }

  /**
   * 源码编译
   *
   * @param packageName 包名
   * @param className 类名
   * @param code 源码
   * @return class
   */
  public Class<?> compile(String packageName, String className, String code) {
    String name = packageName + Strings.DOT + className;
    try {
      return ClassUtils.forName(name, loader);
    } catch (WrappedException e) {
      if (e.getException() instanceof ClassNotFoundException) {
        return doCompile(name, className, code);
      }
      throw e;
    }
  }

  private synchronized Class<?> doCompile(String name, String className, String code) {
    SourceCode sourceCode = new SourceCode(className, code);
    Boolean result = compiler.getTask(null, manager, collector, options, null,
        Collections.singletonList(sourceCode)).call();
    if (result == null || !result) {
      List<Diagnostic<? extends JavaFileObject>> diagnostics = collector.flush();
      log.error("Compilation failed. {}", diagnostics);
      throw new IllegalStateException(
          "Compilation failed. class:" + name + ",diagnostics: " + diagnostics);
    }
    try {
      return loader.loadClass(name);
    } catch (ClassNotFoundException e) {
      throw Exceptions.wrap(e);
    }
  }
}
