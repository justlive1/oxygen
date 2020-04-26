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

package vip.justlive.oxygen.core.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.Hex;
import vip.justlive.oxygen.core.util.MoreObjects;

/**
 * mac encoder
 *
 * @author wubo
 */
@Getter
public class MacEncoder extends BaseEncoder {

  private final String algorithm;
  private final byte[] secret;

  private boolean useBase64 = false;

  public MacEncoder(String algorithm, String secret) {
    this(algorithm, MoreObjects.notNull(secret).getBytes(StandardCharsets.UTF_8));
  }

  public MacEncoder(String algorithm, byte[] secret) {
    this.algorithm = MoreObjects.notNull(algorithm);
    this.secret = MoreObjects.notNull(secret);
  }

  /**
   * 是否使用base64加密
   *
   * @param useBase64 true为使用base64
   * @return encoder
   */
  public MacEncoder useBase64(boolean useBase64) {
    this.useBase64 = useBase64;
    return this;
  }

  @Override
  protected String doEncode(String source) {
    byte[] data = create().doFinal(source.getBytes(StandardCharsets.UTF_8));
    if (useBase64) {
      return Base64.getEncoder().encodeToString(data);
    }
    return Hex.encodeToString(data);
  }

  private Mac create() {
    try {
      Mac mac = Mac.getInstance(algorithm);
      mac.init(new SecretKeySpec(secret, algorithm));
      return mac;
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw Exceptions.wrap(e);
    }
  }
}
