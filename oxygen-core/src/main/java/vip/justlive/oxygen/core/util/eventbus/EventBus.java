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

import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.Naming;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 事件总线
 *
 * @author wubo
 */
@Slf4j
public class EventBus implements Naming {

    private final String name;
    private final Executor executor;
    private final EventExceptionHandler exceptionHandler;
    private final Dispatcher dispatcher;

    public EventBus() {
        this("default");
    }

    public EventBus(String name) {
        this(name, ThreadUtils.newThreadPool(1, 4, 120, 100000, "eb-" + name + "-%d"), LogEventExceptionHandlerImpl.INS, new ImmediateDispatcher());
    }

    public EventBus(String name, Executor executor, EventExceptionHandler exceptionHandler) {
        this(name, executor, exceptionHandler, new ImmediateDispatcher());
    }

    public EventBus(String name, Executor executor, EventExceptionHandler exceptionHandler, Dispatcher dispatcher) {
        this.name = name;
        this.executor = executor;
        this.exceptionHandler = exceptionHandler;
        this.dispatcher = dispatcher;
        this.dispatcher.setEventBus(this);
    }

    /**
     * 注册监听
     *
     * @param listener 监听器
     */
    public void register(Object listener) {
        for (Map.Entry<Class<?>, List<Subscriber>> entry : findSubscribers(listener).entrySet()) {
            dispatcher.register(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 注销监听
     *
     * @param listener 监听器
     */
    public void unregister(Object listener) {
        for (Map.Entry<Class<?>, List<Subscriber>> entry : findSubscribers(listener).entrySet()) {
            dispatcher.unregister(entry.getKey(), entry.getValue());
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
     * @param event   事件
     */
    public void post(String channel, Object event) {
        dispatcher.dispatch(channel, event);
    }

    @Override
    public String name() {
        return name;
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
                    throw Exceptions.fail(String.format("Subscriber method [%s] must have exactly 1 parameter", method));
                }
                listenerSubscribers.computeIfAbsent(method.getParameterTypes()[0], k -> new ArrayList<>()).add(new Subscriber(this, subscribe.pattern(), listener, method));
            }
            type = type.getSuperclass();
        }
        return listenerSubscribers;
    }

}
