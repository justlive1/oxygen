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
package vip.justlive.oxygen.core.job;


import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.bean.Singleton;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;

/**
 * @author wubo
 */
class JobPluginTest {

  @Test
  void test() {
    Singleton.set(new Conf());
    JobPlugin plugin = new JobPlugin();
    plugin.start();
    ThreadUtils.sleep(10000);
    plugin.stop();
  }
}