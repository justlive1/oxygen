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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import lombok.Getter;
import vip.justlive.oxygen.core.exception.Exceptions;

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

  public Subscriber(EventBus eventBus, String pattern, Object target, Method method) {
    this.eventBus = eventBus;
    this.pattern = pattern;
    this.target = target;
    this.method = method;
    this.method.setAccessible(true);
  }

  void handle(String channel, Object event) {
    EventContext ctx = new EventContext(channel, event, eventBus, this);
    eventBus.handleEvent(ctx);
  }

  void invoke(EventContext ctx) {
    try {
      method.invoke(target, ctx.getEvent());
    } catch (IllegalArgumentException e) {
      throw Exceptions.fault(e, "Method rejected target/argument: " + ctx.getEvent());
    } catch (IllegalAccessException e) {
      throw Exceptions.fault(e, "Method became inaccessible: " + ctx.getEvent());
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof Error) {
        throw (Error) e.getCause();
      }
      eventBus.handleException(e.getCause(), ctx);
    }
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
