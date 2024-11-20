/*
 * Copyright (C) 2023 the original author or authors.
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

import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.util.base.PathMatcher;
import vip.justlive.oxygen.core.util.base.Strings;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 事件调度器
 *
 * @author wubo
 */
@RequiredArgsConstructor
public abstract class Dispatcher {

    protected final ConcurrentMap<Class<?>, CopyOnWriteArraySet<Subscriber>> subscribers = new ConcurrentHashMap<>();

    protected EventBus eventBus;


    /**
     * 分发事件
     *
     * @param channel 渠道
     * @param event   事件
     */
    public abstract void dispatch(String channel, Object event);

    void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    void register(Class<?> clazz, List<Subscriber> sbs) {
        subscribers.computeIfAbsent(clazz, k -> new CopyOnWriteArraySet<>()).addAll(sbs);
    }

    void unregister(Class<?> clazz, List<Subscriber> sbs) {
        CopyOnWriteArraySet<Subscriber> list = subscribers.get(clazz);
        if (list != null) {
            list.removeAll(sbs);
        }
    }

    protected List<Subscriber> getSubscribers(String channel, Object event) {
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

    static Set<Class<?>> getRawTypes(Class<?> clazz) {
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
}
