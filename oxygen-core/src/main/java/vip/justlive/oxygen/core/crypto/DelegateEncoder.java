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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.Strings;

/**
 * 代理加密
 *
 * @author wubo
 */
public class DelegateEncoder extends BaseEncoder {

  private static final String ERROR_TEMPLATE = "There is no encoder mapped for the id \"%s\"";

  private static final Map<String, Encoder> ENCODERS = new ConcurrentHashMap<>();

  static {
    ENCODERS.put("NoOp", new NoOpEncoder());
    ENCODERS.put("MD5", new Md5Encoder());
    ENCODERS.put("SHA-1", new MessageDigestEncoder("SHA-1"));
    ENCODERS.put("SHA-256", new MessageDigestEncoder("SHA-256"));
  }

  private final String id;

  public DelegateEncoder(String id) {
    if (!ENCODERS.containsKey(id)) {
      throw new IllegalArgumentException(String.format(ERROR_TEMPLATE, id));
    }
    this.id = id;
  }

  @Override
  public String encode(String source) {
    String salt = Strings.OPEN_BRACE.concat(id).concat(Strings.CLOSE_BRACE);
    return salt.concat(doEncode(source));
  }

  @Override
  protected String doEncode(String source) {
    Encoder encoder = ENCODERS.get(id);
    return encoder.encode(source);
  }

  @Override
  public boolean match(String source, String raw) {
    String salt = extractSalt(raw);
    Encoder encoder = ENCODERS.get(salt);
    if (encoder == null) {
      throw Exceptions.fail(String.format(ERROR_TEMPLATE, salt));
    }
    String value = Strings.OPEN_BRACE.concat(salt).concat(Strings.CLOSE_BRACE)
        .concat(encoder.encode(source));
    return Objects.equals(value, raw);
  }

  @Override
  public void setUseSalt(boolean useSalt) {
    throw new UnsupportedOperationException();
  }
}
