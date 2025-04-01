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
package vip.justlive.oxygen.core.util.scan;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ResolvedModule;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.JarFileInfo;
import vip.justlive.oxygen.core.util.base.PathMatcher;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.base.Urls;

/**
 * 默认class scanner实现，采用类加载方式
 *
 * @author wubo
 */
@Slf4j
public class DefaultClassScanner implements ClassScanner {

  private static final String CLASS_SUFFIX = ".class";
  private static final String CLASS_PATH_MULTI_SUFFIX = "/**/*" + CLASS_SUFFIX;

  private static final Map<String, Set<Class<?>>> CACHE = new HashMap<>(2);

  /**
   * 扫描包路径
   */
  private final ClassLoader loader;

  public DefaultClassScanner() {
    this(ClassUtils.getDefaultClassLoader());
  }

  public DefaultClassScanner(ClassLoader loader) {
    this.loader = loader;
  }

  @Override
  public Set<Class<?>> scan(String... packages) {
    Set<Class<?>> classes = new HashSet<>();
    for (String pkg : packages) {
      String transfer = pkg.replace(Strings.DOT, Strings.SLASH) + CLASS_PATH_MULTI_SUFFIX;
      try {
        this.findMatchPath(transfer, classes);
      } catch (IOException e) {
        log.warn("scan package [{}] -> [{}] failed", pkg, transfer, e);
      }
    }
    return classes;
  }

  private void findMatchPath(String location, Set<Class<?>> classes) throws IOException {
    Set<Class<?>> result = CACHE.get(location);
    if (result == null) {
      result = new HashSet<>(2);
      // Search the module path first.
      findAllModulePath(location, classes);
      // Search the class path next.
      String rootPath = PathMatcher.getRootDir(location);
      String subPattern = location.substring(rootPath.length());
      Enumeration<URL> urls = this.loader.getResources(Urls.cutRootPath(rootPath));
      while (urls.hasMoreElements()) {
        URL rootUrl = urls.nextElement();
        if (Urls.URL_PROTOCOL_FILE.equals(rootUrl.getProtocol())) {
          this.findFileMatchPath(rootPath, rootUrl, subPattern, result);
        } else if (Urls.isJarURL(rootUrl)) {
          this.findJarMatchPath(rootUrl, subPattern, result);
        }
      }
      CACHE.put(location, result);
    }
    classes.addAll(result);
  }

  private static final Set<String> SYSTEM_MODULE_NAMES = ModuleFinder.ofSystem().findAll().stream()
      .map(moduleReference -> moduleReference.descriptor().name()).collect(Collectors.toSet());
  private static final Predicate<ResolvedModule> IS_NOT_SYSTEM_MODULE = resolvedModule -> !SYSTEM_MODULE_NAMES.contains(
      resolvedModule.name());

  private void findAllModulePath(String location, Set<Class<?>> classes) {
    final String resourcePattern;
    if (location.startsWith(Strings.SLASH)) {
      resourcePattern = location.substring(Strings.SLASH.length());
    } else {
      resourcePattern = location;
    }
    Predicate<String> resourcePatternMatches = (PathMatcher.isPattern(resourcePattern)
        ? path -> PathMatcher.match(resourcePattern, path) : resourcePattern::equals);
    ModuleLayer.boot().configuration().modules().stream().filter(IS_NOT_SYSTEM_MODULE)
        .forEach(resolvedModule -> {
          log.info("resolved module {}", resolvedModule);
          // NOTE: a ModuleReader and a Stream returned from ModuleReader.list() must be closed.
          try (ModuleReader moduleReader = resolvedModule.reference()
              .open(); Stream<String> names = moduleReader.list()) {
            names.filter(resourcePatternMatches)
                .map(name -> findModuleResource(moduleReader, name, resolvedModule))
                .filter(Objects::nonNull).forEach(classes::add);
          } catch (IOException ex) {
            if (log.isDebugEnabled()) {
              log.debug("Failed to read contents of module [{}]", resolvedModule, ex);
            }
            throw new UncheckedIOException(ex);
          }
        });
  }

  private Class<?> findModuleResource(ModuleReader moduleReader, String name,
      ResolvedModule resolvedModule) {
    try {
      return moduleReader.find(name).map(uri -> this.convertModule(uri, resolvedModule))
          .orElse(null);
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to find resource [{}}] in module path", name, e);
      }
      return null;
    }
  }

  private Class<?> convertModule(URI uri, ResolvedModule resolvedModule) {
    URI base = resolvedModule.reference().location().orElse(null);
    String location = "";
    if (base != null) {
      location = base.toString();
    }
    String testLocation = location;
    if (location.endsWith("/target/classes/")) {
      // 测试模式下相同包名归属同一个模块
      testLocation = base.getPath().replace("/target/classes/", "/target/test-classes/");
    }
    String path = uri.toString();
    if (Urls.URL_PROTOCOL_FILE.equals(uri.getScheme())) {
      if (path.startsWith(location)) {
        path = path.substring(location.length());
      } else if (uri.getPath().startsWith(testLocation)) {
        path = uri.getPath().substring(testLocation.length());
      }
      return loadClass(
          path.replace(Strings.SLASH, Strings.DOT).replace(CLASS_SUFFIX, Strings.EMPTY));
    } else {
      String prefix = uri.getScheme() + Strings.COLON + location + Urls.JAR_URL_SEPARATOR;
      if (path.startsWith(prefix)) {
        path = path.substring(prefix.length());
        return loadClass(
            path.replace(Strings.SLASH, Strings.DOT).replace(CLASS_SUFFIX, Strings.EMPTY));
      }
      log.info("load unknown resource from jar module location {} -> {}", location, uri);
    }
    return null;
  }

  private Class<?> loadClass(String className) {
    try {
      return loader.loadClass(className);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      log.warn("class [{}] can not load ", className);
    }
    return null;
  }

  private void findFileMatchPath(String rootPath, URL rootUrl, String subPattern,
      Set<Class<?>> classes) {
    File rootDir;
    try {
      rootDir = new File(Urls.toURI(rootUrl).getSchemeSpecificPart());
    } catch (URISyntaxException e) {
      rootDir = new File(rootUrl.getFile());
    }
    Set<File> matchedFiles = PathMatcher.findMatchedFiles(rootDir, subPattern);
    for (File file : matchedFiles) {
      String className = pathToClassName(rootPath, rootDir, file).replace(CLASS_SUFFIX,
          Strings.EMPTY);
      Class<?> clazz = loadClass(className);
      if (clazz != null) {
        classes.add(clazz);
      }
    }
  }

  private void findJarMatchPath(URL rootUrl, String subPattern, Set<Class<?>> classes)
      throws IOException {
    try (JarFileInfo jarFileInfo = Urls.getJarFileInfo(rootUrl)) {
      if (log.isDebugEnabled()) {
        log.debug("Looking for matching resources in jar file [{}]", jarFileInfo.jarFileUrl);
      }
      String rootEntryPath = jarFileInfo.rootEntryPath;
      if (!rootEntryPath.isEmpty() && !rootEntryPath.endsWith(Strings.SLASH)) {
        rootEntryPath += Strings.SLASH;
      }
      for (Enumeration<JarEntry> entries = jarFileInfo.jarFile.entries();
          entries.hasMoreElements(); ) {
        JarEntry entry = entries.nextElement();
        String entryPath = entry.getName();
        if (entryPath.startsWith(rootEntryPath) && PathMatcher.match(subPattern,
            entryPath.substring(rootEntryPath.length()))) {
          String className = entryPath.replace(Strings.SLASH, Strings.DOT)
              .replace(CLASS_SUFFIX, Strings.EMPTY);
          Class<?> clazz = loadClass(className);
          if (clazz != null) {
            classes.add(clazz);
          }
        }
      }
    }
  }

  private String pathToClassName(String rootPath, File rootDir, File subFile) {
    return pathToClassName(rootPath,
        subFile.getAbsolutePath().replace(rootDir.getAbsolutePath(), Strings.EMPTY)
            .replace(File.separator, Strings.DOT));
  }

  private String pathToClassName(String rootPath, String subPath) {
    String className = rootPath.replace(Strings.SLASH, Strings.DOT);
    if (!className.endsWith(Strings.DOT)) {
      className = className.concat(Strings.DOT);
    }
    if (subPath.startsWith(Strings.DOT)) {
      return className.concat(subPath.substring(1));
    }
    return className.concat(subPath);
  }

}
