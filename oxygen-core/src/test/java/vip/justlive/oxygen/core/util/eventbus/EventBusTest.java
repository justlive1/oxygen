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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * @author wubo
 */
class EventBusTest {

  @Test
  void test0() {

    EventBus eb = new EventBus();
    eb.register(new MySubscriber());
    DeadEs ds = new DeadEs();
    eb.register(ds);
    EchoEvent event = new EchoEvent();
    eb.post("a", event);

    ThreadUtils.sleep(1000);

    assertEquals(3, event.getRes().size());

    event.getRes().clear();

    eb.post("ab", event);

    eb.post("ax", new MsgEvent());
    ThreadUtils.sleep(1000);

    assertEquals(1, ds.deadCount);

  }

  @Test
  void test1() {

    MySubscriber subscriber = new MySubscriber();
    EventBus eb = new EventBus("sync", MoreObjects.directExecutor(),
        LogEventExceptionHandlerImpl.INS);
    eb.register(subscriber);
    DeadEs ds = new DeadEs();
    eb.register(ds);
    EchoEvent event = new EchoEvent();

    eb.post("acc", event);

    assertEquals(2, event.getRes().size());

    event.getRes().clear();

    eb.post("", event);
    eb.post("m3", new MsgEvent());
    assertEquals(0, ds.deadCount);

    eb.unregister(subscriber);

    eb.post("acc", event);
    eb.post("m3", new MsgEvent());
    assertEquals(2, ds.deadCount);

  }
}
