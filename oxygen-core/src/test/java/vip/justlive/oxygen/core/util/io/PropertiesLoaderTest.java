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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.exception.CodedException;

/**
 * @author wubo
 */
public class PropertiesLoaderTest {

  String classpath = "classpath:/config/*.properties";
  String defaultPath = "/config/config.properties";
  String dir = "/tmp";
  String path = dir + "/config.properties";
  String filePath = "file:" + path;


  @Test
  public void testDefault() {

    PropertiesLoader loader = new PropertiesLoader(defaultPath);
    loader.init();

    assertEquals("k001", loader.getProperty("k1"));
    assertEquals("k002", loader.getProperty("k2"));

  }

  @Test
  public void testClasspath() {

    PropertiesLoader loader = new PropertiesLoader(classpath);
    loader.init();

    assertEquals("k001", loader.getProperty("k1"));
    assertEquals("k002", loader.getProperty("k2"));
    assertEquals("k003", loader.getProperty("k3"));
    assertEquals("k003", loader.getProperty("k4"));
    assertEquals("k003", loader.getProperty("k5"));
    assertEquals("k006", loader.getProperty("k6"));
    assertEquals("k006", loader.getProperty("k7"));


  }

  @Test
  public void testFile() {

    makeFile();

    PropertiesLoader loader = new PropertiesLoader(filePath);
    loader.init();

    assertEquals("k001", loader.getProperty("k1"));
    assertEquals("k002", loader.getProperty("k2"));

  }

  @Test(expected = CodedException.class)
  public void testNotFound() {
    PropertiesLoader loader = new PropertiesLoader("classpath:/xxx.properties");
    loader.init();
  }

  @Test
  public void testIgnoreNotFound() {
    PropertiesLoader loader = new PropertiesLoader("classpath:/xxx.properties");
    loader.ignoreNotFound = true;
    loader.init();
    Assert.assertTrue(loader.props().isEmpty());
  }

  void makeFile() {
    File dir = new File(this.dir);
    File target = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    if (target.exists()) {
      target.delete();
    }
    ClassPathResource resource = new ClassPathResource(defaultPath);
    try {
      Files.copy(resource.getInputStream(), target.toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
    target.deleteOnExit();
  }

}