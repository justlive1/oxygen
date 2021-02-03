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

package vip.justlive.oxygen.core.util.crypto;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author wubo
 */
public class MacEncoderTest {

  @Test
  public void t0() {

    String algorithm = "HmacSHA256";
    String secret = "123xcef";
    String source = "123456qweasd";
    MacEncoder encoder = new MacEncoder(algorithm, secret);
    String result = encoder.encode(source);

    Assert.assertEquals("fafea2c76d82df6d21edb318754afe57f25a62b05ad4fa9f427a0b4cb9b498d7", result);

    result = encoder.useBase64(true).encode(source);
    Assert.assertEquals("+v6ix22C320h7bMYdUr+V/JaYrBa1PqfQnoLTLm0mNc=", result);

  }
}