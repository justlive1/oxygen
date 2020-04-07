/*
 * Copyright (C) 2019 the original author or authors.
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

package vip.justlive.oxygen.core.config;

import java.util.Properties;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.net.aio.core.Server;

/**
 * @author wubo
 */
public class BinderTest {

  static Properties props() {
    Properties prop = new Properties();
    prop.setProperty("f1", "1");
    prop.setProperty("f2", "2");
    prop.setProperty("12", "3");

    prop.setProperty("lc.f1", "4");
    prop.setProperty("lc.f2", "5");
    prop.setProperty("lc.f4.port", "5");

    prop.setProperty("rp.f1", "6");
    prop.setProperty("rp.f2", "7");
    prop.setProperty("rp.f3.f1", "7");
    prop.setProperty("rp.f3.f2", "7");

    return prop;
  }

  @Test
  public void bind() {
    Binder binder = new Binder(BinderTest::props);
    Obj obj = binder.bind(Obj.class);
    Assert.assertEquals("1", obj.f1);
    Assert.assertEquals("3", obj.f2);

    Obj1 obj1 = binder.bind(Obj1.class);
    Assert.assertEquals("4", obj1.f1);
    Assert.assertEquals("3", obj1.f2);

  }

  @Test
  public void testBind() {
    Binder binder = new Binder(BinderTest::props);
    Obj obj = binder.bind(Obj.class, "lc");
    Assert.assertEquals("4", obj.f1);
    Assert.assertEquals("3", obj.f2);

    Obj1 obj1 = binder.bind(Obj1.class, "rp");
    Assert.assertEquals("6", obj1.f1);
    Assert.assertEquals("3", obj1.f2);
    Assert.assertNotNull(obj1.f3);
    Assert.assertEquals("7", obj1.f3.f1);
    Assert.assertEquals("3", obj1.f3.f2);
  }

  @Test
  public void testBind1() {
    Binder binder = new Binder(BinderTest::props);
    Obj obj = new Obj();
    obj.f1 = "8";
    obj.f2 = "9";
    binder.bind(obj, "lc");
    Assert.assertEquals("4", obj.f1);
    Assert.assertEquals("3", obj.f2);

    Obj obj1 = new Obj();
    obj1.f1 = "8";
    obj1.f2 = "9";
    binder.bind(obj1, "rp");
    Assert.assertEquals("6", obj1.f1);
    Assert.assertEquals("3", obj1.f2);

  }

  @Test
  public void testArray() {
    Properties prop = new Properties();
    prop.setProperty("f1", "1");
    prop.setProperty("f2", "2");
    prop.setProperty("f3", "3,1");

    Binder binder = new Binder(() -> prop);
    Obj obj = binder.bind(Obj.class);
    Assert.assertNotNull(obj.f3);
    Assert.assertEquals(2, obj.f3.length);
    Assert.assertEquals(new Integer(3), obj.f3[0]);
  }

  @Data
  static class Obj {

    private String f1;
    @Value("${12}")
    private String f2;
    private Integer[] f3;
  }

  @Data
  @ValueConfig("lc")
  static class Obj1 {

    private String f1;
    @Value("${12}")
    private String f2;

    private Obj f3;
    @Ignore
    private Server f4;
  }
}