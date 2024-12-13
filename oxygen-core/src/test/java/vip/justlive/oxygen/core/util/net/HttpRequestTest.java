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

package vip.justlive.oxygen.core.util.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.net.http.HttpRequest;
import vip.justlive.oxygen.core.util.net.http.HttpRequestHolder;
import vip.justlive.oxygen.core.util.net.http.HttpResponse;
import vip.justlive.oxygen.core.util.net.http.HttpResponseErrorHandlerImpl;

/**
 * @author wubo
 */
class HttpRequestTest {

  @Test
  void test0() throws IOException {
    AtomicInteger count = new AtomicInteger();
    try (HttpResponse response = HttpRequest.get(
            "https://cn.bing.com/AS/Suggestions?pt=page.serp&bq=Charset&mkt=zh-cn&ds=mobileweb&qry=charset&cp=7&cvid=5E954FAA1B634A69A7CCF9438379A606")
        .charset(StandardCharsets.UTF_8).connectTimeout(1000).readTimeout(1000)
        .interceptors(Collections.singletonList(new TestHttpRequestInterceptor(count)))
        .followRedirects(false).execute()) {
      assertEquals(200, response.getCode());
    }

    assertEquals(1, count.get());
  }

  //  @Test
  void test1() throws IOException {
    HttpResponse response = HttpRequest.post("http://localhost:8080/upload/2")
        .charset(StandardCharsets.UTF_8).connectTimeout(1000).readTimeout(1000)
        .followRedirects(false).queryParam(new Body().mobile("1").captcha("2"))
        .addHeader("token", "zzz")
        .multipart("a", "134134")
        .multipart("f1", new File("/tmp/a123.properties"))
        .execute();
    assertEquals(200, response.getCode());
    System.out.println(response.bodyAsString());
  }

  //  @Test
  void test2() throws IOException {
    HttpRequest request = HttpRequest.post("http://1.w2wz.com/upload.php")
        .charset(StandardCharsets.UTF_8)
        .followRedirects(false);
    request.multipart("uploadimg", new File("/tmp/backImage.jpg"));

    HttpResponse response = request.execute();
    assertEquals(200, response.getCode());
    String res = response.bodyAsString();
    assertNotNull(res);

    System.out.println(res);
  }

  //  @Test
  void test3() throws IOException {
    HttpResponse response = HttpRequest.post("http://localhost:8080/upload/3")
        .charset(StandardCharsets.UTF_8).connectTimeout(1000).readTimeout(1000)
        .followRedirects(false).queryParam(new Body().mobile("1").captcha("2"))
        .addHeader("token", "zzz")
        .jsonBody(MoreObjects.mapOf("ax", 1, "er", 23, "adg", "xdga"))
        .execute();
    assertEquals(200, response.getCode());
    System.out.println(response.bodyAsString());
  }

  //  @Test
  void test4() throws IOException {
    HttpResponse response = HttpRequest.post("http://localhost:8080/upload/3")
        .charset(StandardCharsets.UTF_8).connectTimeout(1000).readTimeout(1000)
        .followRedirects(false).queryParam(new Body().mobile("1").captcha("2"))
        .formBody(MoreObjects.mapOf("token", "三个", "123", 2))
        .execute();
    assertEquals(200, response.getCode());
    System.out.println(response.bodyAsString());
  }

  @Test
  void test5() {
    try (HttpResponse response = HttpRequest.get(
            "https://gitee.com/ld/1")
        .execute()) {
      assertEquals(404, response.getCode());
    } catch (IOException e) {
      e.printStackTrace();
    }

    try (HttpResponse response = HttpRequest.get(
            "https://gitee.com/ld/1").httpResponseErrorHandler(HttpResponseErrorHandlerImpl.HANDLER)
        .execute()) {
    } catch (CodedException e) {
      assertEquals("404", e.getErrorCode().getCode());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void test6() {
    try (HttpResponse response = HttpRequest.get(
            "https://gitee.com/ld/1")
        .execute()) {
      assertEquals(404, response.getCode());

      HttpRequestHolder holder = HttpRequestHolder.get();

      assertNotNull(holder);

      assertEquals(response, holder.getResponse());

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Data
  @Accessors(fluent = true)
  private static class Body {

    private String mobile;
    private String captcha;
  }
}
