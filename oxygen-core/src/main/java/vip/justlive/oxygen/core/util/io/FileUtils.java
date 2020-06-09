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

package vip.justlive.oxygen.core.util.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.CoreConfigKeys;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.SnowflakeId;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.net.http.HttpRequest;
import vip.justlive.oxygen.core.util.net.http.HttpResponse;

/**
 * 文件工具类
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class FileUtils {

  private final Properties MIME_TYPES = new Properties();

  static {
    try (InputStream in = FileUtils.class.getResourceAsStream("/mime-types.properties")) {
      MIME_TYPES.load(in);
    } catch (IOException e) {
      log.warn("mime types initial failed ", e);
    }
  }

  /**
   * 创建目录
   *
   * @param dir 目录
   */
  public void mkdirs(File dir) {
    if (dir == null) {
      return;
    }
    if (dir.exists()) {
      log.info("mkdirs [{}] exists", dir);
    } else {
      if (!dir.mkdirs()) {
        throw Exceptions.fail("create dir fail");
      }
      log.info("mkdirs [{}] successfully", dir);
    }
  }

  /**
   * 创建目录
   *
   * @param path 路径
   */
  public void mkdirs(Path path) {
    if (path == null) {
      return;
    }
    mkdirs(path.toFile());
  }

  /**
   * 创建目录
   *
   * @param path 路径
   */
  public void mkdirs(String path) {
    if (path == null) {
      return;
    }
    mkdirs(new File(path));
  }

  /**
   * 创建多级目录
   *
   * @param parent 父目录
   * @param children 子目录
   */
  public void mkdirs(String parent, String... children) {
    MoreObjects.notNull(parent);
    File parentDir = new File(parent);
    if (children != null && children.length > 0) {
      for (String child : children) {
        parentDir = new File(parentDir, child);
      }
    }
    mkdirs(parentDir);
  }

  /**
   * 判断文件父目录是否存在，不存在则创建
   *
   * @param file 文件
   */
  public void mkdirsForFile(File file) {
    if (file == null) {
      return;
    }
    File dir = file.getParentFile();
    if (dir != null && !dir.exists()) {
      mkdirs(dir);
    }
  }

  /**
   * 创建文件
   *
   * @param file 文件
   */
  public void touch(File file) {
    if (file == null) {
      return;
    }
    if (!file.exists()) {
      mkdirsForFile(file);
      try {
        if (!file.createNewFile()) {
          throw Exceptions.fail("touch file fail");
        }
        log.info("file [{}] created", file);
      } catch (IOException e) {
        throw Exceptions.wrap(e);
      }
    } else {
      log.info("file [{}] exists", file);
    }
  }

  /**
   * 创建文件
   *
   * @param path 文件路径
   */
  public void touch(Path path) {
    if (path == null) {
      return;
    }
    touch(path.toFile());
  }

  /**
   * 创建文件
   *
   * @param filepath 文件路径
   */
  public void touch(String filepath) {
    if (filepath == null) {
      return;
    }
    touch(new File(filepath));
  }

  /**
   * 获取文件扩展名
   *
   * @param filename 文件名
   * @return extension
   */
  public String extension(String filename) {
    int index = MoreObjects.notNull(filename).lastIndexOf(Strings.DOT);
    if (index == -1) {
      return Strings.EMPTY;
    }
    return filename.substring(index + 1);
  }

  /**
   * count files
   *
   * @param file 文件
   * @return count
   */
  public int countFiles(File file) {
    return countFiles(file, MoreObjects.alwaysTrue());
  }

  /**
   * count files
   *
   * @param file 文件
   * @param filter 过滤器
   * @return count
   */
  public int countFiles(File file, Predicate<File> filter) {
    MoreObjects.notNull(file, "file cant not be null");
    MoreObjects.notNull(filter, "filter can not be null");
    if (file.isFile()) {
      if (filter.test(file)) {
        return 1;
      }
      return 0;
    } else {
      File[] files = file.listFiles();
      if (files == null || files.length == 0) {
        return 0;
      }
      int count = 0;
      for (File child : files) {
        count += countFiles(child, filter);
      }
      return count;
    }
  }

  /**
   * count dirs
   *
   * @param file 文件
   * @return count
   */
  public int countDirs(File file) {
    return countDirs(file, MoreObjects.alwaysTrue());
  }

  /**
   * count dirs
   *
   * @param file 文件
   * @param filter 过滤器
   * @return count
   */
  public int countDirs(File file, Predicate<File> filter) {
    MoreObjects.notNull(file, "file cant not be null");
    MoreObjects.notNull(filter, "filter can not be null");
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files == null || files.length == 0) {
        if (filter.test(file)) {
          return 1;
        }
        return 0;
      }
      int count = 1;
      for (File child : files) {
        count += countDirs(child, filter);
      }
      return count;
    } else {
      return 0;
    }
  }

  /**
   * 获取文件绝对路径
   *
   * @param file 文件
   * @return path
   */
  public String absolutePath(File file) {
    MoreObjects.notNull(file);
    try {
      return file.getCanonicalPath();
    } catch (IOException e) {
      return file.getAbsolutePath();
    }
  }

  /**
   * 是否是相同的文件路径
   *
   * @param file 文件
   * @param another 另一文件
   * @return true为相同路径
   */
  public boolean isSamePath(File file, File another) {
    if (file == another || file.equals(another)) {
      return true;
    }
    return absolutePath(file).equals(absolutePath(another));
  }

  /**
   * 判断是否为根目录
   *
   * @param dir 目录
   * @return true为根目录
   */
  public boolean isRoot(File dir) {
    if (dir == null || !dir.isDirectory()) {
      return false;
    }
    for (File file : File.listRoots()) {
      if (isSamePath(file, dir)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 删除文件或目录
   *
   * @param file 文件
   * @return count
   */
  public int delete(File file) {
    if (file != null && file.exists()) {
      if (file.isFile()) {
        return deleteFile(file);
      } else {
        return deleteDir(file);
      }
    }
    return 0;
  }

  /**
   * 删除文件
   *
   * @param file 文件
   * @return count
   */
  public int deleteFile(File file) {
    if (file != null && file.isFile()) {
      return deletePath(file.toPath());
    }
    return 0;
  }

  /**
   * 删除目录
   *
   * @param file 目录
   * @return count
   */
  public int deleteDir(File file) {
    if (file != null && file.isDirectory()) {
      File[] files = file.listFiles();
      int count = 0;
      if (files != null && files.length > 0) {
        for (File subFile : files) {
          if (subFile.isFile()) {
            count += deleteFile(subFile);
          } else {
            count += deleteDir(subFile);
          }
        }
      }
      count += deletePath(file.toPath());
      return count;
    }
    return 0;
  }

  /**
   * 获取配置或系统的临时文件夹目录
   *
   * @return temp dir
   */
  public File tempDir() {
    String tempDir = CoreConfigKeys.TEMP_DIR.getValue();
    if (!Strings.hasText(tempDir)) {
      try {
        tempDir = System.getProperty("java.io.tmpdir");
      } catch (Exception e) {
        tempDir = ".oxygen";
      }
    }
    return new File(tempDir);
  }

  /**
   * 获取项目的临时文件根目录,比tempDir多了oxygen/version目录
   *
   * @return temp base dir
   */
  public File tempBaseDir() {
    return new File(tempDir(), Bootstrap.version());
  }

  /**
   * 清理项目临时目录
   *
   * @return 删除的文件和目录数
   */
  public int cleanTempBaseDir() {
    return deleteDir(tempBaseDir());
  }

  /**
   * 创建临时目录
   *
   * @param first 第一层目录
   * @param children 字目录
   * @return 目录
   */
  public File createTempDir(String first, String... children) {
    File dir = new File(tempBaseDir(), first);
    if (children.length > 0) {
      for (String child : children) {
        dir = new File(dir, child);
      }
    }
    if (dir.isDirectory()) {
      return dir;
    } else if (dir.exists()) {
      throw Exceptions.fail("the existed file is not directory: " + first);
    }
    mkdirs(dir);
    return dir;
  }

  /**
   * 通过文件名称解析扩展名得到媒体类型
   *
   * @param filename 文件名
   * @return mime-type
   */
  public String parseMimeType(String filename) {
    return getMimeType(extension(filename));
  }

  /**
   * 通过扩展名得到媒体类型
   *
   * @param extension 扩展名
   * @return mime-type
   */
  public String getMimeType(String extension) {
    return MIME_TYPES.getProperty(extension);
  }

  /**
   * 下载文件
   *
   * @param url 文件地址
   * @return 文件
   * @throws IOException io异常
   */
  public File download(String url) throws IOException {
    File file = new File(FileUtils.tempBaseDir(),
        SnowflakeId.defaultNextId() + Strings.DOT + FileUtils.extension(url));
    download(url, file);
    return file;
  }

  /**
   * 下载文件
   *
   * @param url 文件地址
   * @param file 文件
   * @throws IOException io异常
   */
  public void download(String url, File file) throws IOException {
    try (HttpResponse response = HttpRequest.get(url)
        .execute(); FileOutputStream out = new FileOutputStream(file)) {
      IoUtils.copy(response.getBody(), out);
    }
  }

  private int deletePath(Path path) {
    try {
      Files.delete(path);
      return 1;
    } catch (IOException e) {
      log.warn("delete file error", e);
    }
    return 0;
  }
}
