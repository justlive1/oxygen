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
package vip.justlive.oxygen.web.undertow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.util.base.SystemUtils;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;
import vip.justlive.oxygen.core.util.net.http.HttpMethod;
import vip.justlive.oxygen.core.util.net.http.HttpRequest;
import vip.justlive.oxygen.core.util.net.http.HttpResponse;
import vip.justlive.oxygen.web.router.Router;
import vip.justlive.oxygen.web.server.Server;

/**
 * @author wubo
 */
class UndertowWebServerTest {

  @Test
  void test() {

    String msg = "hello world";
    int port = SystemUtils.findAvailablePort();

    Router.router().method(HttpMethod.GET).path("/a").handler(ctx -> ctx.response().write(msg));
    Router.router().method(HttpMethod.POST).path("/a")
        .handler(ctx -> ctx.response().write(msg + "!"));
    Server.listen(port);

    ThreadUtils.sleep(3000);

    try (HttpResponse response = HttpRequest.get("http://localhost:" + port + "/a").execute()) {
      assertEquals(msg, response.bodyAsString());
    } catch (Exception e) {
      fail();
    }

    try (HttpResponse response = HttpRequest.post("http://localhost:" + port + "/a").execute()) {
      assertEquals(msg + "!", response.bodyAsString());
    } catch (Exception e) {
      fail();
    }

    try (HttpResponse response = HttpRequest.post("http://localhost:" + port + "/b/123")
        .execute()) {
      assertEquals("123", response.bodyAsString());
    } catch (Exception e) {
      fail();
    }

    Server.stop();
  }
}