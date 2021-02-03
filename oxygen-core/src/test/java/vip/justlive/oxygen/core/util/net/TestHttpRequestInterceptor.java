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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.util.net.http.HttpRequest;
import vip.justlive.oxygen.core.util.net.http.HttpRequestExecution;
import vip.justlive.oxygen.core.util.net.http.HttpRequestInterceptor;
import vip.justlive.oxygen.core.util.net.http.HttpResponse;

/**
 * @author wubo
 */
@RequiredArgsConstructor
public class TestHttpRequestInterceptor implements HttpRequestInterceptor {

  private final AtomicInteger count;

  @Override
  public HttpResponse intercept(HttpRequest request, HttpRequestExecution execution)
      throws IOException {
    count.incrementAndGet();
    return execution.execute(request);
  }
}
