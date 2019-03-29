/*
 *  Copyright (C) 2019 justlive1
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
package vip.justlive.oxygen.web.http;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.web.WebPlugin;

/**
 * session parse
 *
 * @author wubo
 */
public class SessionRequestParse implements RequestParse {

  @Override
  public boolean supported(HttpServletRequest req) {
    return true;
  }

  @Override
  public void handle(HttpServletRequest req) {
    Request request = Request.current();
    Session session = getSession(request);
    if (session == null) {
      session = new Session();
      session.setId(UUID.randomUUID().toString());
      WebPlugin.SESSION_MANAGER.createSession(session);
    }
    request.session = session;
  }

  private Session getSession(Request request) {
    String id = request.getCookieValue(Constants.SESSION_COOKIE_KEY);
    if (id != null) {
      return WebPlugin.SESSION_MANAGER.getSession(id);
    }
    return null;
  }
}
