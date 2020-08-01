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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.Naming;
import vip.justlive.oxygen.core.util.base.PathMatcher;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * 事件总线
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class EventBus implements Naming {

  private final String name;
  private final Executor executor;
  private final EventExceptionHandler exceptionHandler;
  private final ConcurrentMap<Class<?>, CopyOnWriteArraySet<Subscriber>> subscribers = new ConcurrentHashMap<>();

  public EventBus() {
    this("default");
  }

  public EventBus(String name) {
    this(name, ThreadUtils.newThreadPool(1, 4, 120, 100000, "eb-" + name + "-%d"),
        LogEventExceptionHandlerImpl.INS);
  }

  /**
   * 注册监听
   *
   * @param listener 监听器
   */
  public void register(Object listener) {
    for (Map.Entry<Class<?>, List<Subscriber>> entry : findSubscribers(listener).entrySet()) {
      subscribers.computeIfAbsent(entry.getKey(), k -> new CopyOnWriteArraySet<>())
          .addAll(entry.getValue());
    }
  }

  /**
   * 注销监听
   *
   * @param listener 监听器
   */
  public void unregister(Object listener) {
    for (Map.Entry<Class<?>, List<Subscriber>> entry : findSubscribers(listener).entrySet()) {
      CopyOnWriteArraySet<Subscriber> list = subscribers.get(entry.getKey());
      if (list != null) {
        list.removeAll(entry.getValue());
      }
    }
  }

  /**
   * 触发事件（所有订阅者）
   *
   * @param event 事件
   */
  public void post(Object event) {
    post(Strings.ANY, event);
  }

  /**
   * 根据渠道触发事件
   *
   * @param channel 渠道
   * @param event 事件
   */
  public void post(String channel, Object event) {
    List<Subscriber> list = getSubscribers(channel, event);
    if (!list.isEmpty()) {
      list.forEach(subscriber -> subscriber.handle(channel, event));
    } else if (!(event instanceof DeadEvent)) {
      post(new DeadEvent(channel, event, this));
    }
  }

  @Override
  public String name() {
    return name;
  }

  List<Subscriber> getSubscribers(String channel, Object event) {
    List<Subscriber> list = new ArrayList<>();
    for (Class<?> type : getRawTypes(event.getClass())) {
      CopyOnWriteArraySet<Subscriber> subscriberList = subscribers.get(type);
      if (subscriberList == null) {
        continue;
      }
      for (Subscriber subscriber : subscriberList) {
        if (Strings.ANY.equals(channel) || PathMatcher.match(subscriber.getPattern(), channel)) {
          list.add(subscriber);
        }
      }
    }
    return list;
  }


  void handleException(Throwable e, EventContext ctx) {
    try {
      exceptionHandler.handle(e, ctx);
    } catch (Throwable throwable) {
      log.error("EventExceptionHandler throw Exception", throwable);
    }
  }

  void handleEvent(EventContext ctx) {
    executor.execute(() -> {
      EventContext.set(ctx);
      try {
        ctx.getSubscriber().invoke(ctx);
      } finally {
        EventContext.remove();
      }
    });
  }

  private Set<Class<?>> getRawTypes(Class<?> clazz) {
    Set<Class<?>> types = new HashSet<>();
    Class<?> type = clazz;
    while (type != null && type != Object.class) {
      types.add(type);
      Class<?>[] interfaces = type.getInterfaces();
      if (interfaces.length > 0) {
        types.addAll(Arrays.asList(interfaces));
      }
      type = type.getSuperclass();
    }
    return types;
  }

  private Map<Class<?>, List<Subscriber>> findSubscribers(Object listener) {
    Map<Class<?>, List<Subscriber>> listenerSubscribers = new HashMap<>(4);
    Class<?> type = listener.getClass();
    while (type != null && type != Object.class) {
      for (Method method : type.getDeclaredMethods()) {
        Subscribe subscribe = method.getAnnotation(Subscribe.class);
        if (method.isSynthetic() || subscribe == null) {
          continue;
        }
        if (method.getParameterTypes().length != 1) {
          throw Exceptions
              .fail(String.format("Subscriber method [%s] must have exactly 1 parameter", method));
        }
        listenerSubscribers.computeIfAbsent(method.getParameterTypes()[0], k -> new ArrayList<>())
            .add(new Subscriber(this, subscribe.pattern(), listener, method));
      }
      type = type.getSuperclass();
    }
    return listenerSubscribers;
  }

}
