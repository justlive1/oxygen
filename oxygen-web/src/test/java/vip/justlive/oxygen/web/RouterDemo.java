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

import vip.justlive.oxygen.core.util.net.http.HttpMethod;
import vip.justlive.oxygen.web.annotation.Mapping;
import vip.justlive.oxygen.web.annotation.PathParam;
import vip.justlive.oxygen.web.annotation.Router;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * @author wubo
 */
@Router("/b")
public class RouterDemo {

  @Mapping(method = HttpMethod.POST, value = "/{id}")
  public void id(@PathParam("id") String id, RoutingContext ctx) {
    ctx.response().write(id);
  }
}
