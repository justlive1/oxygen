/*
 * Copyright (C) 2019 justlive1
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

package vip.justlive.oxygen.core.net.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author wubo
 */
public class HttpRequestTest {

  @Test
  public void test0() throws IOException {
    try (HttpResponse response = HttpRequest.get(
        "https://cn.bing.com/AS/Suggestions?pt=page.serp&bq=Charset&mkt=zh-cn&ds=mobileweb&qry=charset&cp=7&cvid=5E954FAA1B634A69A7CCF9438379A606")
        .charset(StandardCharsets.UTF_8).connectTimeout(1000).readTimeout(1000)
        .followRedirects(false).execute()) {
      Assert.assertEquals(200, response.getCode());
    }
  }

  public void test1() throws IOException {
    HttpResponse response = HttpRequest.post("http://localhost:8080/api/login/validate")
        .charset(StandardCharsets.UTF_8).connectTimeout(1000).readTimeout(1000)
        .followRedirects(false).queryParam(new Body().mobile("1").captcha("2"))
        .jsonBody("{\"a\":1}").addHeader("token", "zzz").execute();
    Assert.assertEquals(200, response.getCode());
    Assert.assertNotNull(response.bodyAsString());
  }

  @Data
  @Accessors(fluent = true)
  private static class Body {

    private String mobile;
    private String captcha;
  }
}