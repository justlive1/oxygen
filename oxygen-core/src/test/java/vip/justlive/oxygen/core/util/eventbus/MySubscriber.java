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

/**
 * @author wubo
 */
@Slf4j
public class MySubscriber {


  @Subscribe(pattern = "a")
  public void echo0(EchoEvent event) {
    log.info("enter into [a]-{}", event);
    event.getRes().add("a");
  }

  @Subscribe
  public void echo1(EchoEvent event) {
    log.info("enter into [*]-{}", event);
    event.getRes().add("*");
  }

  @Subscribe(pattern = "a*")
  public void echo2(EchoEvent event) {
    log.info("enter into [a*]-{}", event);
    event.getRes().add("a*");
  }

  @Subscribe(pattern = "m*")
  public void msg(MsgEvent event) {
    log.info("enter into [m*]-{}", event);
  }


}
