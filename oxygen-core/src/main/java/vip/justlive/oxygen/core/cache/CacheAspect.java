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
package vip.justlive.oxygen.core.cache;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import vip.justlive.oxygen.core.aop.Aspect;
import vip.justlive.oxygen.core.aop.Aspect.TYPE;
import vip.justlive.oxygen.core.aop.Invocation;
import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.bean.Destroy;
import vip.justlive.oxygen.core.bean.Initialize;
import vip.justlive.oxygen.core.bean.Singleton;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * cache切面
 *
 * @author wubo
 */
@Bean
@RequiredArgsConstructor
public class CacheAspect {

  private static final Cache CACHE = new LocalCacheImpl("Aspect-Cache");

  private KeyGenerator keyGenerator;

  @Initialize
  public void init() {
    keyGenerator = Singleton.get(KeyGenerator.class);
    if (keyGenerator == null) {
      keyGenerator = new KeyGeneratorImpl();
    }
  }

  @Destroy
  public void destroy() {
    CACHE.clear();
  }

  @Aspect(annotation = Cacheable.class, type = TYPE.BEFORE)
  public boolean cacheRead(Invocation invocation) {
    Ctx ctx = parse(invocation);
    Cache cache = Cache.cache(ctx.cacheName);
    Object obj = cache.get(ctx.key);
    if (obj != null) {
      invocation.setReturnValue(obj);
      return false;
    }
    return true;
  }

  @Aspect(annotation = Cacheable.class, type = TYPE.AFTER)
  public boolean cacheWrite(Invocation invocation) {
    Ctx ctx = parse(invocation);
    Object obj = Cache.cache(ctx.cacheName).get(ctx.key);
    if (!Objects.equals(obj, invocation.getReturnValue())) {
      if (ctx.duration > 0) {
        Cache.cache(ctx.cacheName)
            .set(ctx.key, invocation.getReturnValue(), ctx.duration, ctx.unit);
      } else {
        Cache.cache(ctx.cacheName).set(ctx.key, invocation.getReturnValue());
      }
    }
    return true;
  }

  private Ctx parse(Invocation invocation) {
    String cacheKey = String.join(Strings.COLON, invocation.getTarget().getClass().getName(),
        invocation.getMethod().getName(), Arrays.deepToString(invocation.getArgs()));
    Ctx ctx = CACHE.get(cacheKey, Ctx.class);
    if (ctx != null) {
      return ctx;
    }
    ctx = new Ctx();
    Cacheable cacheable = invocation.getMethod().getAnnotation(Cacheable.class);
    String cacheName = cacheable.value();
    if (cacheName.length() == 0) {
      cacheName = Cache.class.getSimpleName();
    }
    ctx.cacheName = cacheName;
    ctx.key = keyGenerator.generate(cacheable, invocation);
    ctx.duration = cacheable.duration();
    ctx.unit = cacheable.timeUnit();
    CACHE.set(cacheKey, ctx, 10, TimeUnit.MINUTES);
    return ctx;
  }

  @ToString
  static class Ctx {

    String key;
    String cacheName;
    long duration;
    TimeUnit unit;
  }

}
