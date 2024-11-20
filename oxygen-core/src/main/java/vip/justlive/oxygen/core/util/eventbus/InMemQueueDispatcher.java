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
import vip.justlive.oxygen.core.util.concurrent.RepeatRunnable;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 内存队列事件分发器
 *
 * @author wubo
 */
public class InMemQueueDispatcher extends ImmediateDispatcher {

    private final BlockingQueue<EventWrapper> queue = new LinkedBlockingQueue<>();

    public InMemQueueDispatcher() {
        ThreadUtils.globalPool().execute(new RepeatRunnable("IMQDispatcher", this::run));
    }

    @Override
    public void dispatch(String channel, Object event) {
        queue.add(new EventWrapper(channel, event));
    }

    private void run() {
        EventWrapper eventWrapper = null;
        try {
            eventWrapper = queue.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (eventWrapper != null) {
            super.dispatch(eventWrapper.channel, eventWrapper.event);
        }
    }

    @RequiredArgsConstructor
    private static class EventWrapper {
        final String channel;
        final Object event;
    }
}
