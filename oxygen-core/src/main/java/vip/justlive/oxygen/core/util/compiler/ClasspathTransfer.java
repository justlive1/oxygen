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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.base.Urls;
import vip.justlive.oxygen.core.util.io.FileUtils;

/**
 * class path 转换，目前只处理fat-jar
 *
 * @author wubo
 */
public class ClasspathTransfer {

  private static final String FAT_JAR_SEPARATOR = "!/";
  private static final File ROOT;

  static {
    ROOT = FileUtils.createTempDir("compiler", "classpath");
  }

  public void transfer(List<File> files) {
    List<File> result = new ArrayList<>(files.size());
    for (File file : files) {
      if (file.exists()) {
        result.add(file);
        continue;
      }
      String path = file.getPath().replace(File.separator, Strings.SLASH);
      int index = path.indexOf(FAT_JAR_SEPARATOR);
      if (index > -1) {
        fatJar(path.substring(0, index), path.substring(index + FAT_JAR_SEPARATOR.length()),
            result);
      } else if (path.endsWith(Strings.EXCLAMATION_MARK)) {
        result.add(new File(path.substring(Strings.FILE_PREFIX.length(), path.length() - 1)));
      }
    }
    files.clear();
    files.addAll(result);
    result.clear();
  }

  private void fatJar(String prefix, String suffix, List<File> result) {
    try (JarFile jar = Urls.getJarFile(prefix)) {
      fatJar(jar, suffix, result);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private void fatJar(JarFile jar, String path, List<File> result) throws IOException {
    int index = path.indexOf(FAT_JAR_SEPARATOR);
    if (index > -1) {
      String prefix = path.substring(0, index);
      String suffix = path.substring(index + FAT_JAR_SEPARATOR.length());
      File target = new File(ROOT, prefix);
      FileUtils.mkdirsForFile(target);
      JarEntry entry = jar.getJarEntry(prefix);
      Files.copy(jar.getInputStream(entry), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
      fatJar(target.getAbsolutePath(), suffix, result);
    } else {
      if (path.endsWith(Strings.EXCLAMATION_MARK)) {
        path = path.substring(0, path.length() - 1);
      }
      File target = new File(ROOT, path);
      FileUtils.mkdirsForFile(target);
      Files.copy(jar.getInputStream(jar.getEntry(path)), target.toPath(),
          StandardCopyOption.REPLACE_EXISTING);
      result.add(target);
    }
  }

}
