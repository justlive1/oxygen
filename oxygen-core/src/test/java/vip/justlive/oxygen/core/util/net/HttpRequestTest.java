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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.net.http.HttpRequest;
import vip.justlive.oxygen.core.util.net.http.HttpResponse;

/**
 * @author wubo
 */
public class HttpRequestTest {

  @Test
  public void test0() throws IOException {
    AtomicInteger count = new AtomicInteger();
    try (HttpResponse response = HttpRequest.get(
        "https://cn.bing.com/AS/Suggestions?pt=page.serp&bq=Charset&mkt=zh-cn&ds=mobileweb&qry=charset&cp=7&cvid=5E954FAA1B634A69A7CCF9438379A606")
        .charset(StandardCharsets.UTF_8).connectTimeout(1000).readTimeout(1000)
        .interceptors(Collections.singletonList(new TestHttpRequestInterceptor(count)))
        .followRedirects(false).execute()) {
      Assert.assertEquals(200, response.getCode());
    }

    Assert.assertEquals(1, count.get());
  }

  //  @Test
  public void test1() throws IOException {
    HttpResponse response = HttpRequest.post("http://localhost:8080/upload/2")
        .charset(StandardCharsets.UTF_8).connectTimeout(1000).readTimeout(1000)
        .followRedirects(false).queryParam(new Body().mobile("1").captcha("2"))
        .addHeader("token", "zzz")
        .multipart("a", "134134")
        .multipart("f1", new File("/tmp/a123.properties"))
        .execute();
    Assert.assertEquals(200, response.getCode());
    System.out.println(response.bodyAsString());
  }

  //  @Test
  public void test2() throws IOException {
    HttpRequest request = HttpRequest.post("http://1.w2wz.com/upload.php")
        .charset(StandardCharsets.UTF_8)
        .followRedirects(false);
    request.multipart("uploadimg", new File("/tmp/backImage.jpg"));

    HttpResponse response = request.execute();
    Assert.assertEquals(200, response.getCode());
    String res = response.bodyAsString();
    Assert.assertNotNull(res);

    System.out.println(res);
  }

  //  @Test
  public void test3() throws IOException {
    HttpResponse response = HttpRequest.post("http://localhost:8080/upload/3")
        .charset(StandardCharsets.UTF_8).connectTimeout(1000).readTimeout(1000)
        .followRedirects(false).queryParam(new Body().mobile("1").captcha("2"))
        .addHeader("token", "zzz")
        .jsonBody(MoreObjects.mapOf("ax", 1, "er", 23, "adg", "xdga"))
        .execute();
    Assert.assertEquals(200, response.getCode());
    System.out.println(response.bodyAsString());
  }

  //  @Test
  public void test4() throws IOException {
    HttpResponse response = HttpRequest.post("http://localhost:8080/upload/3")
        .charset(StandardCharsets.UTF_8).connectTimeout(1000).readTimeout(1000)
        .followRedirects(false).queryParam(new Body().mobile("1").captcha("2"))
        .formBody(MoreObjects.mapOf("token", "三个", "123", 2))
        .execute();
    Assert.assertEquals(200, response.getCode());
    System.out.println(response.bodyAsString());
  }

  @Test
  public void test5() {
    try (HttpResponse response = HttpRequest.get(
        "https://gitee.com/ld/1")
        .execute()) {
      Assert.assertEquals(404, response.getCode());
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
