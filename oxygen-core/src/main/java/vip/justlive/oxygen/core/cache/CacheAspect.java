/*
 * Copyright (C) 2018 justlive1
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
import vip.justlive.oxygen.core.aop.After;
import vip.justlive.oxygen.core.aop.Before;
import vip.justlive.oxygen.core.aop.Invocation;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.ioc.Bean;
import vip.justlive.oxygen.core.ioc.Inject;

/**
 * cache切面
 *
 * @author wubo
 */
@Bean
public class CacheAspect {

  private final DefaultKeyGenerator defaultKeyGenerator;
  private final ArgsKeyGenerator argsKeyGenerator;
  private static final Cache CACHE = new LocalCacheImpl();

  @Inject
  public CacheAspect(DefaultKeyGenerator defaultKeyGenerator,
      ArgsKeyGenerator argsKeyGenerator) {
    this.defaultKeyGenerator = defaultKeyGenerator;
    this.argsKeyGenerator = argsKeyGenerator;
  }

  @Before(annotation = Cacheable.class)
  public boolean cacheRead(Invocation invocation) {
    Ctx ctx = parse(invocation);
    Cache cache = JCache.cache(ctx.cacheName);
    Object obj = cache.get(ctx.key);
    if (obj != null) {
      invocation.setReturnValue(obj);
      return false;
    }
    return true;
  }

  @After(annotation = Cacheable.class)
  public boolean cacheWrite(Invocation invocation) {
    Ctx ctx = parse(invocation);
    Object obj = JCache.cache(ctx.cacheName).get(ctx.key);
    if (!Objects.equals(obj, invocation.getReturnValue())) {
      if (ctx.duration > 0) {
        JCache.cache(ctx.cacheName)
            .set(ctx.key, invocation.getReturnValue(), ctx.duration, ctx.unit);
      } else {
        JCache.cache(ctx.cacheName).set(ctx.key, invocation.getReturnValue());
      }
    }
    return true;
  }

  Ctx parse(Invocation invocation) {
    String cacheKey = String.join(Constants.COLON, invocation.getMethod().getName(),
        Arrays.deepToString(invocation.getArgs()));
    Ctx ctx = (Ctx) CACHE.get(cacheKey);
    if (ctx != null) {
      return ctx;
    }
    ctx = new Ctx();
    Cacheable cacheable = invocation.getMethod().getAnnotation(Cacheable.class);
    String cacheName = cacheable.value();
    if (cacheName.length() == 0) {
      cacheName = JCache.class.getSimpleName();
    }
    String key = cacheable.key();
    if (key.length() == 0) {
      key = defaultKeyGenerator
          .generate(invocation.getTarget(), invocation.getMethod(), invocation.getArgs())
          .toString();
    } else {
      key = argsKeyGenerator
          .generate(invocation.getTarget(), invocation.getMethod(), key, invocation.getArgs())
          .toString();
    }
    ctx.cacheName = cacheName;
    ctx.key = key;
    ctx.duration = cacheable.duration();
    ctx.unit = cacheable.timeUnit();
    CACHE.set(cacheKey, ctx, 10, TimeUnit.MINUTES);
    return ctx;
  }

  class Ctx {

    String key;
    String cacheName;
    long duration;
    TimeUnit unit;
  }

}
