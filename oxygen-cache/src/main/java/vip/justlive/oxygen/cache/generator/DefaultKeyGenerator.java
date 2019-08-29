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
package vip.justlive.oxygen.cache.generator;

import java.lang.reflect.Method;
import java.util.Arrays;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.crypto.Encoder;
import vip.justlive.oxygen.core.crypto.Md5Encoder;
import vip.justlive.oxygen.ioc.annotation.Bean;

/**
 * 默认缓存key生成器
 *
 * @author wubo
 */
@Bean
public class DefaultKeyGenerator implements KeyGenerator {

  private static Encoder encoder = new Md5Encoder();

  @Override
  public Object generate(Object target, Method method, Object... params) {
    String src;
    if (params.length > 0) {
      src = String.join(Constants.DOT, method.getDeclaringClass().getName(), method.getName(),
          Arrays.deepToString(params));
    } else {
      src = String.join(Constants.DOT, method.getDeclaringClass().getName(), method.getName());
    }
    return encoder.encode(src);
  }
}


