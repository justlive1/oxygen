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
package vip.justlive.oxygen.core.util.eventbus;

import java.lang.reflect.Method;
import java.util.Objects;
import lombok.Getter;
import vip.justlive.oxygen.core.aop.invoke.Invoker;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.exception.WrappedException;
import vip.justlive.oxygen.core.util.base.ClassUtils;

/**
 * 订阅包装
 *
 * @author wubo
 */
@Getter
public class Subscriber {

  private final EventBus eventBus;
  private final String pattern;
  private final Object target;
  private final Method method;
  private final Invoker invoker;

  public Subscriber(EventBus eventBus, String pattern, Object target, Method method) {
    this.eventBus = eventBus;
    this.pattern = pattern;
    this.target = target;
    this.method = method;
    this.invoker = ClassUtils.generateInvoker(target, method);
  }

  void handle(String channel, Object event) {
    EventContext ctx = new EventContext(channel, event, eventBus, this);
    eventBus.handleEvent(ctx);
  }

  void invoke(EventContext ctx) {
    Throwable exception = null;
    try {
      invoker.invoke(new Object[]{ctx.getEvent()});
    } catch (WrappedException e) {
      exception = e.getException();
    } catch (Exception e) {
      exception = e;
    }
    if (exception == null) {
      return;
    }
    if (exception instanceof IllegalArgumentException) {
      throw Exceptions.fault(exception, "Method rejected target/argument: " + ctx.getEvent());
    } else if (exception instanceof IllegalAccessException) {
      throw Exceptions.fault(exception, "Method became inaccessible: " + ctx.getEvent());
    } else if (exception instanceof Error) {
      throw (Error) exception.getCause();
    }
    eventBus.handleException(exception, ctx);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Subscriber that = (Subscriber) o;
    return Objects.equals(pattern, that.pattern) && Objects.equals(target, that.target) && Objects
        .equals(method, that.method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pattern, target, method);
  }
}
