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

package vip.justlive.oxygen.core.util;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 捕获异常的consumer
 *
 * @param <T> 泛型
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class CaughtConsumer<T> implements Consumer<T> {

  private final Consumer<T> consumer;

  @Override
  public void accept(T t) {
    try {
      consumer.accept(t);
    } catch (Exception e) {
      log.error("consumer error", e);
    }
  }

  @Override
  public CaughtConsumer<T> andThen(Consumer<? super T> after) {
    return new CaughtConsumer<>(Consumer.super.andThen(after));
  }
}
