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

package vip.justlive.oxygen.web.hook;

import vip.justlive.oxygen.ioc.annotation.Bean;
import vip.justlive.oxygen.web.http.Multipart;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * 文件上传清理
 *
 * @author wubo
 */
@Bean
public class MultipartCleanerHook implements WebHook {

  @Override
  public void finished(RoutingContext ctx) {
    Multipart multipart = ctx.request().getMultipart();
    if (multipart != null) {
      multipart.getCleaner().close();
    }
  }
}
