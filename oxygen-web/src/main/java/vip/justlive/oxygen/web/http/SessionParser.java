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
package vip.justlive.oxygen.web.http;

import java.util.UUID;
import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.web.Context;

/**
 * session parse
 *
 * @author wubo
 */
@Bean
public class SessionParser implements Parser {

  @Override
  public int order() {
    return 140;
  }

  @Override
  public void parse(Request request) {
    Session session = getSession(request);
    if (session == null) {
      session = new Session();
      session.setId(UUID.randomUUID().toString());
      Context.SESSION_MANAGER.createSession(session);
    }
    request.session = session;
  }

  private Session getSession(Request request) {
    String id = request.getCookieValue(Session.SESSION_COOKIE_KEY);
    if (id != null) {
      return Context.SESSION_MANAGER.getSession(id);
    }
    return null;
  }
}
