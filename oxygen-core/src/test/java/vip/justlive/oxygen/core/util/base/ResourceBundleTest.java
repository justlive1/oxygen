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
package vip.justlive.oxygen.core.util.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.Bootstrap;

/**
 * @author wubo
 */
 class ResourceBundleTest {

  @BeforeAll
  public static void before() {
    Bootstrap.start();
  }

  @Test
   void getMessage() {
    assertEquals("key_zh_CN", ResourceBundle.getMessage("i18n.k1"));
    ResourceBundle.setThreadLocale(new Locale("en", "US"));
    assertEquals("key_en_US", ResourceBundle.getMessage("i18n.k1"));
    ResourceBundle.setThreadLocale(new Locale("en"));
    assertEquals("key", ResourceBundle.getMessage("i18n.k1"));

  }

  @Test
   void getMessage1() {
    assertEquals("key2_en_US", ResourceBundle.getMessage("i18n.k2", new Locale("en", "US")));
    assertNull(ResourceBundle.getMessage("i18n.k2"));
  }

  @Test
   void getMessage2() {
    assertEquals("key2_en_US", ResourceBundle.getMessage("i18n.k2", "en", "US"));
    assertNull(ResourceBundle.getMessage("i18n.k2"));
  }
}