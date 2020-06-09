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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import org.junit.Assert;
import org.junit.Test;

public class FileUtilsTest {

  @Test
  public void mkdirs1() {
    String name = "/tmp/a/1/1";
    File file = new File(name);
    FileUtils.mkdirs(file);
    assertTrue(file.exists());
    assertTrue(file.isDirectory());
    name = "/tmp/a/2/2";
    file = new File(name);
    FileUtils.mkdirs(name);
    assertTrue(file.exists());
    assertTrue(file.isDirectory());
    name = "/tmp/a/3/3";
    file = new File(name);
    FileUtils.mkdirs(Paths.get(name));
    assertTrue(file.exists());
    assertTrue(file.isDirectory());
    file = new File("/tmp/a");
    assertEquals(0, FileUtils.countFiles(file));
    assertEquals(7, FileUtils.countDirs(file));
    assertEquals(7, FileUtils.delete(file));
  }

  @Test
  public void mkdirs2() {
    String parent = "/tmp/a/4";
    FileUtils.mkdirs(parent, "1", "2");
    File file = new File(parent + "/1/2");
    assertTrue(file.exists());
    assertTrue(file.isDirectory());
    assertEquals(4, FileUtils.delete(new File("/tmp/a")));
  }


  @Test
  public void mkdirsForFile() {
    FileUtils.mkdirsForFile(new File("/tmp/a/5/a.log"));
    File file = new File("/tmp/a/5");
    assertTrue(file.exists());
    assertTrue(file.isDirectory());
    assertEquals(2, FileUtils.delete(new File("/tmp/a")));
  }

  @Test
  public void touch() {
    FileUtils.touch("/tmp/a/6/a.log");
    File file = new File("/tmp/a/6/a.log");
    assertTrue(file.exists());
    assertTrue(file.isFile());
    assertEquals(3, FileUtils.delete(new File("/tmp/a")));
  }


  @Test
  public void extension() {
    assertEquals("txt", FileUtils.extension("a.txt"));
  }

  @Test
  public void t0() {
    Predicate<File> filter = file -> file.getName().endsWith(".log");
    assertEquals(0, FileUtils.countDirs(new File("/tmp/a/7")));
    assertEquals(0, FileUtils.countFiles(new File("/tmp/a/7")));
    FileUtils.mkdirs("/tmp/a/7");
    assertEquals(1, FileUtils.countDirs(new File("/tmp/a/7")));
    assertEquals(0, FileUtils.countDirs(new File("/tmp/a/7"), filter));
    assertEquals(0, FileUtils.countFiles(new File("/tmp/a/7")));
    FileUtils.touch("/tmp/a/7/a.log");
    assertEquals(0, FileUtils.countDirs(new File("/tmp/a/7/a.log")));
    assertEquals(1, FileUtils.countFiles(new File("/tmp/a/7/a.log")));
    assertEquals(0, FileUtils.countFiles(new File("/tmp/a/7/a.log"), filter.negate()));
    assertEquals(3, FileUtils.delete(new File("/tmp/a")));
    assertEquals(0, FileUtils.delete(null));
    assertEquals(0, FileUtils.deleteFile(null));
    assertEquals(0, FileUtils.deleteDir(null));

    FileUtils.touch("/tmp/a/7/a.log");
    assertEquals(1, FileUtils.delete(new File("/tmp/a/7/a.log")));
    FileUtils.delete(new File("/tmp/a"));
  }

  @Test
  public void t1() {
    assertFalse(FileUtils.isRoot(null));
    assertFalse(FileUtils.isRoot(new File(".")));
    assertTrue(FileUtils.isRoot(new File("d:/")));

    assertFalse(FileUtils.isSamePath(new File("/tmp/a"), new File("/tmp/a/b")));
    assertTrue(FileUtils.isSamePath(new File("/tmp/a"), new File("/tmp/a/b/..")));
  }

  @Test
  public void t2() {
    FileUtils.mkdirs((File) null);
    FileUtils.mkdirs((String) null);
    FileUtils.mkdirs((Path) null);
    FileUtils.mkdirsForFile(null);
    FileUtils.mkdirs("/tmp/a/8");
    FileUtils.mkdirs("/tmp/a/8");

    FileUtils.touch((Path) null);
    FileUtils.touch((String) null);
    FileUtils.touch(Paths.get("/tmp/a/8/a.log"));
    FileUtils.touch(Paths.get("/tmp/a/8/a.log"));

    Assert.assertTrue(new File("/tmp/a/8/a.log").exists());

    FileUtils.delete(new File("/tmp/a"));

  }

  @Test
  public void testDownload() throws IOException {
    File file = new File("/tmp/baidu.png");
    FileUtils.download("https://www.baidu.com/img/bd_logo1.png", file);
    Assert.assertTrue(file.exists());
    FileUtils.deleteFile(file);
    FileUtils.mkdirs(FileUtils.tempBaseDir());
    file = FileUtils.download("https://www.baidu.com/img/bd_logo1.png");
    FileUtils.deleteFile(file);

  }
}