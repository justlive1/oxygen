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

import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 捕获异常的bi-consumer
 *
 * @param <T> 泛型
 * @param <U> 泛型
 * @author wubo
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class CaughtBiConsumer<T, U> implements BiConsumer<T, U> {

  private final BiConsumer<T, U> consumer;
  private final boolean thrown;

  public CaughtBiConsumer(BiConsumer<T, U> consumer) {
    this(consumer, false);
  }

  @Override
  public void accept(T t, U u) {
    try {
      consumer.accept(t, u);
    } catch (Exception e) {
      if (thrown) {
        throw Exceptions.wrap(e);
      }
      log.error("consumer error", e);
    }
  }

  @Override
  public CaughtBiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
    return new CaughtBiConsumer<>(BiConsumer.super.andThen(after), thrown);
  }
}
