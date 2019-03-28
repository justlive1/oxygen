/*
 *  Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */
package vip.justlive.oxygen.aop.proxy;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wubo
 */
public class NoLogService {

  static AtomicInteger ato = new AtomicInteger(1);

  @NoLog
  public void log() {
    System.out.println("enter into no log service");
    ato.set(ato.get() * 100 + 1);
  }
}
