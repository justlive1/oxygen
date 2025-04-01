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

package vip.justlive.oxygen.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import vip.justlive.oxygen.core.CoreConfigKeys;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.base.SystemUtils;
import vip.justlive.oxygen.core.util.concurrent.ExecutorPool;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;
import vip.justlive.oxygen.core.util.net.http.HttpMethod;
import vip.justlive.oxygen.core.util.net.http.HttpRequest;
import vip.justlive.oxygen.core.util.net.http.HttpResponse;
import vip.justlive.oxygen.web.router.Router;
import vip.justlive.oxygen.web.server.Server;

/**
 * @author wubo
 */
@Slf4j
class WebTest {


  @Test
  void test() {

    ConfigFactory.setProperty(CoreConfigKeys.AOP_ENABLED.getName(), "false");

    String msg = "hello world";
    int port = SystemUtils.findAvailablePort();

    Router.router().method(HttpMethod.GET).path("/a").handler(ctx -> {
      log.info("enter into router GET a");
      ctx.response().write(msg);
      log.info("leave router GET a");
    });
    Router.router().method(HttpMethod.POST).path("/a")
        .handler(ctx -> {
          log.info("enter into router POST a");
          ctx.response().write(msg + "!");
        });
    Server.listen(port);

    ThreadUtils.sleep(3000);

    test0(port, msg);

    try (HttpResponse response = HttpRequest.post("http://localhost:" + port + "/b/123")
        .execute()) {
      assertEquals("123", response.bodyAsString());
    } catch (Exception e) {
      fail();
    }

    Server.stop();
  }

  @Test
  void test2() throws InterruptedException {

    ConfigFactory.setProperty(CoreConfigKeys.HTTP_REQUEST_EXECUTION.getName(), "AIO");

    String msg = "hello world";
    int port = 48481;

    ExecutorPool<Void> pool = new ExecutorPool<>(
        ThreadUtils.newThreadPool(1, 100, 120, 10000, "aaa-%d"));
    for (int i = 0; i < 100; i++) {
      pool.submit(() -> test0(port, msg));
    }

    pool.waitForAll();

    System.out.println(111);

  }


  void test0(int port, String msg) {
    log.info("ent1");
    try (HttpResponse response = HttpRequest.get("http://localhost:" + port + "/a").execute()) {
      assertEquals(msg, response.bodyAsString());
      log.info("ent2");
    } catch (Exception e) {
      fail();
    }

    MDC.put(Strings.TRACE_ID, ThreadUtils.nextTraceId());

    log.info("ent3");
    try (HttpResponse response = HttpRequest.post("http://localhost:" + port + "/a")
        .execute()) {
      log.info("ent4");
      assertEquals(msg + "!", response.bodyAsString());
    } catch (Exception e) {
      fail();
    }

    MDC.remove(Strings.TRACE_ID);
  }
}