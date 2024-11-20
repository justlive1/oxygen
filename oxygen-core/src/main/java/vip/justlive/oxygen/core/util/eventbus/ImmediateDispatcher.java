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

import java.util.List;

/**
 * 立即分发事件调度器
 *
 * @author wubo
 */
public class ImmediateDispatcher extends Dispatcher {

    @Override
    public void dispatch(String channel, Object event) {
        List<Subscriber> list = getSubscribers(channel, event);
        if (!list.isEmpty()) {
            list.forEach(subscriber -> subscriber.handle(channel, event));
        } else if (!(event instanceof DeadEvent)) {
            eventBus.post(new DeadEvent(channel, event, eventBus));
        }
    }


}
